package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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
        final ArrayList<Message> messages = new ArrayList<>();

        final java.util.HashSet<ModuleIdentifier> seen = new HashSet<>();
        final Deque<ModuleIdentifier> workList = new ArrayDeque<>();
        workList.add(input.mainModuleIdentifier);
        seen.add(input.mainModuleIdentifier);

        final HashSet<ModuleIdentifier> allModuleIdentifiers = new HashSet<>();
        final HashMap<ConstructorSignature, HashSet<ModuleIdentifier>> constructorIndex =
            new HashMap<>();
        final HashMap<StrategySignature, HashSet<ModuleIdentifier>> strategyIndex = new HashMap<>();
        final HashMap<ConstructorSignature, HashSet<ModuleIdentifier>> overlayIndex =
            new HashMap<>();

        final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections =
            new HashMap<>();
        final HashSet<ConstructorSignature> externalConstructors = new HashSet<>();
        final HashSet<StrategySignature> internalStrategies = new HashSet<>();
        final HashSet<StrategySignature> externalStrategies = new HashSet<>();
        final HashSet<StrategySignature> dynamicRules = new HashSet<>();

        final HashMap<ConstructorSignatureMatcher, HashSet<ConstructorSignatureMatcher>>
            overlayUsesConstructors = new HashMap<>();

        do {
            final ModuleIdentifier moduleIdentifier = workList.remove();

            final FrontInput frontInput =
                new FrontInput.Normal(moduleIdentifier, input.strFileGeneratingTasks);
            if(moduleIdentifier.isLibrary()) {
                allModuleIdentifiers.add(moduleIdentifier);
                final ModuleIndex index =
                    PieUtils.requirePartial(context, lib, frontInput, ToModuleIndex.INSTANCE);

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
                for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : index.injections
                    .entrySet()) {
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
                for(Map.Entry<ConstructorSignature, ArrayList<OverlayData>> e : index.overlayData
                    .entrySet()) {
                    Relation.getOrInitialize(overlayIndex, e.getKey(), HashSet::new)
                        .add(moduleIdentifier);
                    final HashSet<ConstructorSignatureMatcher> overlayUsesCons = Relation
                        .getOrInitialize(overlayUsesConstructors,
                            new ConstructorSignatureMatcher(e.getKey()), HashSet::new);
                    for(OverlayData overlayData : e.getValue()) {
                        for(ConstructorSignature usedConstructor : overlayData.usedConstructors) {
                            overlayUsesCons.add(new ConstructorSignatureMatcher(usedConstructor));
                        }
                    }
                }

                final HashSet<ModuleIdentifier> expandedImports =
                    expandImports(context, moduleImportService, index.imports, index.lastModified,
                        messages, input.strFileGeneratingTasks, input.includeDirs);
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

    public static HashSet<ModuleIdentifier> expandImports(ExecContext context,
        IModuleImportService moduleImportService, ArrayList<IStrategoTerm> imports,
        long lastModified, @Nullable ArrayList<Message> messages,
        ArrayList<STask<?>> strFileGeneratingTasks, ArrayList<? extends ResourcePath> includeDirs)
        throws IOException, ExecException {
        final HashSet<ModuleIdentifier> expandedImports = new HashSet<>();
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
        HashMap<ConstructorSignatureMatcher, HashSet<ConstructorSignatureMatcher>> overlayUsesConstructors,
        ArrayList<Message> messages) {
        final Deque<Set<ConstructorSignatureMatcher>> topoSCCs = Algorithms
            .topoSCCs(overlayUsesConstructors.keySet(),
                sig -> overlayUsesConstructors.getOrDefault(sig, new HashSet<>(0)));
        for(Set<ConstructorSignatureMatcher> topoSCC : topoSCCs) {
            final ConstructorSignatureMatcher signature = topoSCC.iterator().next();
            if(topoSCC.size() > 1 || overlayUsesConstructors
                .getOrDefault(signature, new HashSet<>(0)).contains(signature)) {
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
