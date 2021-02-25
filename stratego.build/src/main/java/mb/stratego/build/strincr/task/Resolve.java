package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.ToModuleIndex;
import mb.stratego.build.strincr.function.output.ModuleIndex;
import mb.stratego.build.strincr.message.CyclicOverlay;
import mb.stratego.build.strincr.message.Message;
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
    public final IModuleImportService moduleImportService;

    @Inject public Resolve(Front front, IModuleImportService moduleImportService) {
        this.front = front;
        this.moduleImportService = moduleImportService;
    }

    @Override public GlobalData exec(ExecContext context, ResolveInput input)
        throws IOException, ExecException {
        final ArrayList<Message> messages = new ArrayList<>();

        final LinkedHashSet<IModuleImportService.ModuleIdentifier> seen = new LinkedHashSet<>();
        final Queue<IModuleImportService.ModuleIdentifier> workList = new ArrayDeque<>();
        workList.add(input.mainModuleIdentifier);
        seen.add(input.mainModuleIdentifier);

        final LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers =
            new LinkedHashSet<>();
        final LinkedHashMap<ConstructorSignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            constructorIndex = new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            strategyIndex = new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            overlayIndex = new LinkedHashMap<>();

        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections =
            new LinkedHashMap<>();
        final LinkedHashSet<ConstructorSignature> externalConstructors = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> internalStrategies = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> externalStrategies = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> dynamicRules = new LinkedHashSet<>();

        final LinkedHashMap<ConstructorSignatureMatcher, LinkedHashSet<ConstructorSignatureMatcher>>
            overlayUsesConstructors = new LinkedHashMap<>();

        while(!workList.isEmpty()) {
            final IModuleImportService.ModuleIdentifier moduleIdentifier = workList.remove();
            allModuleIdentifiers.add(moduleIdentifier);

            final FrontInput frontInput =
                new FrontInput.Normal(moduleIdentifier, input.strFileGeneratingTasks,
                    input.includeDirs, input.linkedLibraries);
            final ModuleIndex index =
                PieUtils.requirePartial(context, front, frontInput, ToModuleIndex.INSTANCE);

            for(ConstructorSignature signature : index.constructors) {
                Relation.getOrInitialize(constructorIndex, signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
            }
            for(ConstructorSignature signature : index.externalConstructors) {
                Relation.getOrInitialize(constructorIndex, signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                externalConstructors.add(signature);
            }
            for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : index.injections
                .entrySet()) {
                Relation.getOrInitialize(nonExternalInjections, e.getKey(), ArrayList::new)
                    .addAll(e.getValue());
            }
            for(StrategySignature signature : index.strategies) {
                Relation.getOrInitialize(strategyIndex, signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
            }
            for(StrategySignature signature : index.internalStrategies) {
                Relation.getOrInitialize(strategyIndex, signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                internalStrategies.add(signature);
            }
            for(StrategySignature signature : index.externalStrategies) {
                Relation.getOrInitialize(strategyIndex, signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                externalStrategies.add(signature);
            }
            for(StrategySignature signature : index.dynamicRules) {
                Relation.getOrInitialize(strategyIndex, signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                dynamicRules.add(signature);
            }
            for(Map.Entry<ConstructorSignature, ArrayList<OverlayData>> e : index.overlayData
                .entrySet()) {
                Relation.getOrInitialize(overlayIndex, e.getKey(), LinkedHashSet::new)
                    .add(moduleIdentifier);
                final HashSet<ConstructorSignatureMatcher> overlayUsesCons = Relation
                    .getOrInitialize(overlayUsesConstructors,
                        new ConstructorSignatureMatcher(e.getKey()), LinkedHashSet::new);
                for(OverlayData overlayData : e.getValue()) {
                    for(ConstructorSignature usedConstructor : overlayData.usedConstructors) {
                        overlayUsesCons.add(new ConstructorSignatureMatcher(usedConstructor));
                    }
                }
            }

            final HashSet<IModuleImportService.ModuleIdentifier> imports =
                new HashSet<>(index.imports);
            imports.removeAll(seen);
            workList.addAll(imports);
            seen.addAll(imports);
        }

        checkCyclicOverlays(overlayUsesConstructors, messages);
        return new GlobalData(allModuleIdentifiers, constructorIndex, nonExternalInjections,
            strategyIndex, overlayIndex, externalConstructors, internalStrategies,
            externalStrategies, dynamicRules, messages);
    }

    private void checkCyclicOverlays(
        HashMap<ConstructorSignatureMatcher, LinkedHashSet<ConstructorSignatureMatcher>> overlayUsesConstructors,
        ArrayList<Message> messages) {
        final Deque<Set<ConstructorSignatureMatcher>> topoSCCs = Algorithms
            .topoSCCs(overlayUsesConstructors.keySet(),
                sig -> overlayUsesConstructors.getOrDefault(sig, new LinkedHashSet<>(0)));
        for(Set<ConstructorSignatureMatcher> topoSCC : topoSCCs) {
            final ConstructorSignatureMatcher signature = topoSCC.iterator().next();
            if(topoSCC.size() > 1 || overlayUsesConstructors
                .getOrDefault(signature, new LinkedHashSet<>(0)).contains(signature)) {
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
