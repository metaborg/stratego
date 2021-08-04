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
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
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

        final ArrayList<String> importedStr2LibPackageNames = new ArrayList<>();
        final LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers =
            new LinkedHashSet<>();
        final LinkedHashSet<SortSignature> nonExternalSorts = new LinkedHashSet<>();
        final LinkedHashSet<ConstructorData> nonExternalConstructors = new LinkedHashSet<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            strategyIndex = new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, StrategyType> strategyTypes = new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            overlayIndex = new LinkedHashMap<>();

        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections =
            new LinkedHashMap<>();
        final LinkedHashSet<SortSignature> externalSorts = new LinkedHashSet<>();
        final LinkedHashSet<ConstructorSignature> externalConstructors = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> internalStrategies = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> externalStrategies = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> dynamicRules = new LinkedHashSet<>();
        final LinkedHashSet<OverlayData> overlayData = new LinkedHashSet<>();

        final LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>>
            overlayUsesConstructors = new LinkedHashMap<>();

        long lastModified = 0L;
        while(!workList.isEmpty()) {
            final IModuleImportService.ModuleIdentifier moduleIdentifier = workList.remove();
            allModuleIdentifiers.add(moduleIdentifier);


            final FrontInput frontInput = getFrontInput(input, moduleIdentifier);
            final ModuleIndex index =
                PieUtils.requirePartial(context, front, frontInput, ToModuleIndex.INSTANCE);

            lastModified = Long.max(lastModified, index.lastModified);
            if(index.str2LibPackageName != null) {
                importedStr2LibPackageNames.add(index.str2LibPackageName);
            }
            nonExternalSorts.addAll(index.sorts);
            externalSorts.addAll(index.externalSorts);
            nonExternalConstructors.addAll(index.nonOverlayConstructors);
            externalConstructors.addAll(index.externalConstructors);
            for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : index.injections
                .entrySet()) {
                Relation.getOrInitialize(nonExternalInjections, e.getKey(), ArrayList::new)
                    .addAll(e.getValue());
            }
            for(StrategyFrontData sfd : index.strategies) {
                Relation.getOrInitialize(strategyIndex, sfd.signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                strategyTypes.put(sfd.signature, sfd.type);
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
                final HashSet<ConstructorSignature> overlayUsesCons = Relation
                    .getOrInitialize(overlayUsesConstructors, e.getKey(),
                        LinkedHashSet::new);
                for(OverlayData overlayDatum : e.getValue()) {
                    overlayUsesCons.addAll(overlayDatum.usedConstructors);
                    overlayData.add(overlayDatum);
                }
            }

            messages.addAll(index.messages);

            final HashSet<IModuleImportService.ModuleIdentifier> imports =
                new HashSet<>(index.imports);
            imports.removeAll(seen);
            workList.addAll(imports);
            seen.addAll(imports);
        }

        checkCyclicOverlays(overlayUsesConstructors, messages, lastModified);
        return new GlobalData(allModuleIdentifiers, importedStr2LibPackageNames, overlayIndex,
            nonExternalInjections, strategyIndex, strategyTypes, nonExternalSorts, externalSorts,
            nonExternalConstructors, externalConstructors, internalStrategies, externalStrategies,
            dynamicRules, overlayData, messages, lastModified);
    }

    static FrontInput getFrontInput(ResolveInput input,
        IModuleImportService.ModuleIdentifier moduleIdentifier) {
        if(input.fileOpenInEditor != null && input.fileOpenInEditor.moduleIdentifier.equals(moduleIdentifier)) {
            return input.fileOpenInEditor;
        }
        return new FrontInput.Normal(moduleIdentifier, input.importResolutionInfo,
            input.autoImportStd);
    }

    private void checkCyclicOverlays(
        HashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsesConstructors,
        ArrayList<Message> messages, long lastModified) {
        final Deque<Set<ConstructorSignature>> topoSCCs = Algorithms
            .topoSCCs(overlayUsesConstructors.keySet(),
                sig -> overlayUsesConstructors.getOrDefault(sig, new LinkedHashSet<>(0)));
        for(Set<ConstructorSignature> topoSCC : topoSCCs) {
            final ConstructorSignature signature = topoSCC.iterator().next();
            if(topoSCC.size() > 1 || overlayUsesConstructors
                .getOrDefault(signature, new LinkedHashSet<>(0)).contains(signature)) {
                for(ConstructorSignature sig : topoSCC) {
                    messages.add(new CyclicOverlay(sig, topoSCC, lastModified));
                }
            }
        }
    }

    @Override public String getId() {
        return id;
    }
}
