package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ImportResolution;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.ToModuleIndex;
import mb.stratego.build.strincr.function.output.ModuleIndex;
import mb.stratego.build.strincr.message.CyclicOverlay;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.UnresolvedImport;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.input.ResolveInput;
import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.util.Algorithms;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;

/**
 * Starts at the given "main" Stratego module, calls {@link Front} on it, and resolves the imports
 * transitively from there to discover all modules. Then builds up some {@link GlobalData} from the
 * {@link ModuleData} results of {@link Front} which is used in different places afterwards.
 */
public class Resolve implements TaskDef<ResolveInput, GlobalData> {
    public static final String id = "stratego." + Resolve.class.getSimpleName();

    public final Front front;
    public final Lib lib;
    public final IModuleImportService moduleImportService;

    @Inject public Resolve(Front front, Lib lib, IModuleImportService moduleImportService) {
        this.front = front;
        this.lib = lib;
        this.moduleImportService = moduleImportService;
    }

    @Override public GlobalData exec(ExecContext context, ResolveInput input)
        throws IOException, ExecException {
        final List<Message<?>> messages = new ArrayList<>();

        final java.util.Set<ModuleIdentifier> seen = new HashSet<>();
        final Deque<ModuleIdentifier> workList = new ArrayDeque<>();
        workList.add(input.mainModuleIdentifier);
        seen.add(input.mainModuleIdentifier);

        final Set<ModuleIdentifier> allModuleIdentifiers = new HashSet<>();
        final Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex = new HashMap<>();
        final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex = new HashMap<>();
        final Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex = new HashMap<>();

        final Map<IStrategoTerm, List<IStrategoTerm>> nonExternalInjections = new HashMap<>();
        final Set<ConstructorSignature> externalConstructors = new HashSet<>();
        final Set<StrategySignature> internalStrategies = new HashSet<>();
        final Set<StrategySignature> externalStrategies = new HashSet<>();
        final Set<StrategySignature> dynamicRules = new HashSet<>();

        final Map<ConstructorSignatureMatcher, Set<ConstructorSignatureMatcher>>
            overlayUsesConstructors = new HashMap<>();

        do {
            final ModuleIdentifier moduleIdentifier = workList.remove();

            final FrontInput frontInput = new FrontInput.Normal(moduleIdentifier, input.strFileGeneratingTasks);
            if(moduleIdentifier.isLibrary()) {
                allModuleIdentifiers.add(moduleIdentifier);
                final ModuleIndex index = PieUtils
                    .requirePartial(context, lib, frontInput, ToModuleIndex.INSTANCE);

                for(ConstructorSignature signature : index.externalConstructors) {
                    Relation.getOrInitialize(constructorIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                    externalConstructors.add(signature);
                }
                for(StrategySignature signature : index.externalStrategies) {
                    Relation.getOrInitialize(strategyIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                    externalStrategies.add(signature);
                }
            } else {
                final STask<ModuleData> sTask = front.createSupplier(frontInput);
                allModuleIdentifiers.add(moduleIdentifier);

                final ModuleIndex index =
                    PieUtils.requirePartial(context, sTask, ToModuleIndex.INSTANCE);

                for(ConstructorSignature signature : index.constructors) {
                    Relation.getOrInitialize(constructorIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                }
                for(ConstructorSignature signature : index.externalConstructors) {
                    Relation.getOrInitialize(constructorIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                    externalConstructors.add(signature);
                }
                for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : index.injections.entrySet()) {
                    Relation.getOrInitialize(nonExternalInjections, e.getKey(), ArrayList::new)
                        .addAll(e.getValue());
                }
                for(StrategySignature signature : index.strategies) {
                    Relation.getOrInitialize(strategyIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                }
                for(StrategySignature signature : index.internalStrategies) {
                    Relation.getOrInitialize(strategyIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                    internalStrategies.add(signature);
                }
                for(StrategySignature signature : index.externalStrategies) {
                    Relation.getOrInitialize(strategyIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                    externalStrategies.add(signature);
                }
                for(StrategySignature signature : index.dynamicRules) {
                    Relation.getOrInitialize(strategyIndex, signature, HashSet::new)
                        .add(moduleIdentifier);
                    dynamicRules.add(signature);
                }
                for(Map.Entry<ConstructorSignature, List<OverlayData>> e : index.overlayData
                    .entrySet()) {
                    Relation.getOrInitialize(overlayIndex, e.getKey(), HashSet::new)
                        .add(moduleIdentifier);
                    final Set<ConstructorSignatureMatcher> overlayUsesCons = Relation
                        .getOrInitialize(overlayUsesConstructors,
                            new ConstructorSignatureMatcher(e.getKey()), HashSet::new);
                    for(OverlayData overlayData : e.getValue()) {
                        for(ConstructorSignature usedConstructor : overlayData.usedConstructors) {
                            overlayUsesCons.add(new ConstructorSignatureMatcher(usedConstructor));
                        }
                    }
                }

                final Set<ModuleIdentifier> expandedImports =
                    expandImports(context, moduleImportService, index.imports,
                        index.lastModified, messages, input.strFileGeneratingTasks, input.includeDirs);
                expandedImports.removeAll(seen);
                workList.addAll(expandedImports);
                seen.addAll(expandedImports);
            }
        } while(!workList.isEmpty());

        checkCyclicOverlays(overlayUsesConstructors, messages);
        return new GlobalData(allModuleIdentifiers, constructorIndex, nonExternalInjections,
            strategyIndex, overlayIndex, externalConstructors, internalStrategies,
            externalStrategies, dynamicRules, messages);
    }

    public static Set<ModuleIdentifier> expandImports(ExecContext context,
        IModuleImportService moduleImportService, List<IStrategoTerm> imports, long lastModified,
        @Nullable List<Message<?>> messages, Collection<STask<?>> strFileGeneratingTasks,
        Collection<? extends ResourcePath> includeDirs) throws IOException, ExecException {
        final Set<ModuleIdentifier> expandedImports = new HashSet<>();
        for(IStrategoTerm anImport : imports) {
            final ImportResolution importResolution = moduleImportService
                .resolveImport(context, anImport, strFileGeneratingTasks, includeDirs);
            if(importResolution instanceof IModuleImportService.UnresolvedImport) {
                if(messages != null) {
                    messages.add(new UnresolvedImport(anImport, lastModified));
                }
            } else if(importResolution instanceof IModuleImportService.ResolvedImport) {
                expandedImports
                    .addAll(((IModuleImportService.ResolvedImport) importResolution).modules);
            }
        }
        return expandedImports;
    }

    private void checkCyclicOverlays(
        Map<ConstructorSignatureMatcher, Set<ConstructorSignatureMatcher>> overlayUsesConstructors,
        List<Message<?>> messages) {
        final Deque<Set<ConstructorSignatureMatcher>> topoSCCs = Algorithms
            .topoSCCs(overlayUsesConstructors.keySet(),
                sig -> overlayUsesConstructors.getOrDefault(sig, Collections.emptySet()));
        for(Set<ConstructorSignatureMatcher> topoSCC : topoSCCs) {
            final ConstructorSignatureMatcher signature = topoSCC.iterator().next();
            if(topoSCC.size() > 1 || overlayUsesConstructors
                .getOrDefault(signature, Collections.emptySet()).contains(signature)) {
                long lastModified = 0;
                for(ConstructorSignature sig : topoSCC) {
                    lastModified = Long.max(lastModified, sig.lastModified);
                }
                for(ConstructorSignatureMatcher sig : topoSCC) {
                    messages.add(new CyclicOverlay(sig.wrapped, topoSCC, lastModified));
                }
            }
        }
    }

    @Override public String getId() {
        return id;
    }
}
