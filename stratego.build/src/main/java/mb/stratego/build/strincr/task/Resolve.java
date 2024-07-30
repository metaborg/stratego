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
import java.util.TreeMap;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.function.ToModuleIndex;
import mb.stratego.build.strincr.function.output.ModuleIndex;
import mb.stratego.build.strincr.message.CyclicOverlay;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.type.DuplicateTypeDefinition;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.input.ResolveInput;
import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.util.Algorithms;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;

/**
 * Starts at the given "main" Stratego module, calls {@link Front} on it, and resolves the imports
 * transitively from there to discover all modules. Then builds up some {@link GlobalData} from the
 * {@link ModuleData} results of {@link Front} which is used in different places afterwards.
 */
public class Resolve implements TaskDef<ResolveInput, GlobalData> {
    public static final String id = "stratego." + Resolve.class.getSimpleName();

    public final Front front;
    public final IModuleImportService moduleImportService;
    protected final ITermFactory tf;

    @jakarta.inject.Inject public Resolve(StrIncrContext strContext, Front front, IModuleImportService moduleImportService) {
        this.front = front;
        this.moduleImportService = moduleImportService;
        this.tf = strContext.getFactory();
    }

    @Override public GlobalData exec(ExecContext context, ResolveInput input)
        throws IOException, ExecException {
        if(input.fileOpenInEditor != null) {
            return fileOpenInEditor(context, input);
        }
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
        final LinkedHashMap<StrategySignature, StrategyType> externalStrategyTypes = new LinkedHashMap<>();
        final TreeMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules = new TreeMap<>();
        final LinkedHashSet<ConstructorData> overlayData = new LinkedHashSet<>();
        final LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts =
            new LinkedHashMap<>();

        final LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>>
            overlayUsesConstructors = new LinkedHashMap<>();

        long lastModified = 0L;
        while(!workList.isEmpty()) {
            final IModuleImportService.ModuleIdentifier moduleIdentifier = workList.remove();
            allModuleIdentifiers.add(moduleIdentifier);


            final FrontInput frontInput = getFrontInput(input, moduleIdentifier);
            final ModuleIndex index =
                context.requireMapping(front, frontInput, ToModuleIndex.INSTANCE);

            lastModified = Long.max(lastModified, index.lastModified);

            importedStr2LibPackageNames.addAll(index.str2LibPackageNames);
            nonExternalSorts.addAll(index.sorts);
            externalSorts.addAll(index.externalSorts);
            nonExternalConstructors.addAll(index.nonOverlayConstructors);
            externalConstructors.addAll(index.externalConstructors);
            Relation.putAll(nonExternalInjections, index.injections, ArrayList::new);
            for(StrategyFrontData sfd : index.strategies) {
                Relation.getOrInitialize(strategyIndex, sfd.signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                final @Nullable StrategyType current = strategyTypes.get(sfd.signature);
                if(current == null || current instanceof StrategyType.Standard
                    && sfd.kind.equals(StrategyFrontData.Kind.TypeDefinition)) {
                    strategyTypes.put(sfd.signature, sfd.type);
                    continue;
                }
                if(!(current instanceof StrategyType.Standard)
                    && sfd.kind.equals(StrategyFrontData.Kind.TypeDefinition)) {
                    messages.add(new DuplicateTypeDefinition(current, lastModified));
                    messages.add(new DuplicateTypeDefinition(sfd.signature.getSubterm(0), lastModified));
                }
            }
            for(StrategySignature signature : index.internalStrategies) {
                Relation.getOrInitialize(strategyIndex, signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                internalStrategies.add(signature);
            }
            for(StrategyFrontData sfd : index.externalStrategies) {
                Relation.getOrInitialize(strategyIndex, sfd.signature, LinkedHashSet::new)
                    .add(moduleIdentifier);
                final @Nullable StrategyType current = externalStrategyTypes.get(sfd.signature);
                if(current == null || current instanceof StrategyType.Standard
                    && sfd.kind.equals(StrategyFrontData.Kind.TypeDefinition)) {
                    externalStrategyTypes.put(sfd.signature, sfd.type);
                }
            }
            for(Map.Entry<StrategySignature, TreeSet<StrategySignature>> e : index.dynamicRules.entrySet()) {
                Relation.getOrInitialize(strategyIndex, e.getKey(), LinkedHashSet::new)
                    .add(moduleIdentifier);
                Relation.getOrInitialize(dynamicRules, e.getKey(), TreeSet::new).addAll(e.getValue());
            }
            for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : index.overlayData
                .entrySet()) {
                Relation.getOrInitialize(overlayIndex, e.getKey(), LinkedHashSet::new)
                    .add(moduleIdentifier);
                overlayData.addAll(e.getValue());
            }
            for(Map.Entry<ConstructorSignature, ArrayList<IStrategoTerm>> e : index.overlayAsts
                .entrySet()) {
                Relation.getOrInitialize(overlayIndex, e.getKey(), LinkedHashSet::new)
                    .add(moduleIdentifier);
                Relation.getOrInitialize(overlayAsts, e.getKey(), ArrayList::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<ConstructorSignature, LinkedHashSet<ConstructorSignature>> e : index.overlayUsedConstrs.entrySet()) {
                final HashSet<ConstructorSignature> overlayUsesCons = Relation
                    .getOrInitialize(overlayUsesConstructors, e.getKey(),
                        LinkedHashSet::new);
                overlayUsesCons.addAll(e.getValue());
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
            nonExternalConstructors, externalConstructors, internalStrategies, externalStrategyTypes,
            dynamicRules, overlayData, overlayAsts, messages, lastModified);
    }

    private GlobalData fileOpenInEditor(ExecContext context, ResolveInput input) {
        final GlobalData normalGD = context.require(this, input.withoutOpenFile());
        final ModuleIndex normalMD = context.requireMapping(front, input.fileOpenInEditor.withoutOpenFile(), ToModuleIndex.INSTANCE);
        final ModuleIndex updatedMD = context.requireMapping(front, input.fileOpenInEditor, ToModuleIndex.INSTANCE);
        final ModuleIndex.Diff diff = ModuleIndex.diff(normalMD, updatedMD);
        if(diff.isEqual) {
            return normalGD;
        }
        return applyDiff(normalGD, diff, input.fileOpenInEditor.moduleIdentifier, input.fileOpenInEditor.ast.lastModified);
    }

    private GlobalData applyDiff(GlobalData normalGD, ModuleIndex.Diff diff, ModuleIdentifier moduleIdentifier, long lastModified) {
        final ArrayList<Message> messages = new ArrayList<>();

        final ArrayList<String> importedStr2LibPackageNames = new ArrayList<>(normalGD.importedStr2LibPackageNames);
        final LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers =
            new LinkedHashSet<>(normalGD.allModuleIdentifiers);
        final LinkedHashSet<SortSignature> nonExternalSorts = new LinkedHashSet<>(normalGD.nonExternalSorts);
        final LinkedHashSet<ConstructorData> nonExternalConstructors = new LinkedHashSet<>(normalGD.nonExternalConstructors);
        final LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            strategyIndex = Relation.copy(normalGD.strategyIndex, LinkedHashMap::new, LinkedHashSet::new);
        final LinkedHashMap<StrategySignature, StrategyType> strategyTypes = new LinkedHashMap<>(normalGD.strategyTypes);
        final LinkedHashMap<ConstructorSignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
            overlayIndex = Relation.copy(normalGD.overlayIndex, LinkedHashMap::new, LinkedHashSet::new);

        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections =
            Relation.copy(normalGD.nonExternalInjections, LinkedHashMap::new, ArrayList::new);
        final LinkedHashSet<SortSignature> externalSorts = new LinkedHashSet<>(normalGD.externalSorts);
        final LinkedHashSet<ConstructorSignature> externalConstructors = new LinkedHashSet<>(normalGD.externalConstructors);
        final LinkedHashSet<StrategySignature> internalStrategies = new LinkedHashSet<>(normalGD.internalStrategies);
        final LinkedHashMap<StrategySignature, StrategyType> externalStrategyTypes = new LinkedHashMap<>(normalGD.externalStrategyTypes);
        final TreeMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules =
            Relation.copy(normalGD.dynamicRules, TreeMap::new, TreeSet::new);
        final LinkedHashSet<ConstructorData> overlayData = new LinkedHashSet<>(normalGD.overlayData);
        final LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts =
            Relation.copy(normalGD.overlayAsts, LinkedHashMap::new, ArrayList::new);

        // skipping overlayUsesConstructors, too much bother, let people see that when they save the file.

        lastModified = Long.max(lastModified, normalGD.lastModified);

        // subtractions
        ModuleIndex index = diff.subtractions;

        importedStr2LibPackageNames.removeAll(index.str2LibPackageNames);
        nonExternalSorts.removeAll(index.sorts);
        externalSorts.removeAll(index.externalSorts);
        nonExternalConstructors.removeAll(index.nonOverlayConstructors);
        externalConstructors.removeAll(index.externalConstructors);
        Relation.removeAll(nonExternalInjections, index.injections);
        for(StrategyFrontData sfd : index.strategies) {
            final LinkedHashSet<ModuleIdentifier> leftOverModules =
                Relation.remove(strategyIndex, sfd.signature, moduleIdentifier);
            strategyTypes.remove(sfd.signature);
            if(leftOverModules != null && !leftOverModules.isEmpty()) {
                strategyTypes.put(sfd.signature, sfd.signature.standardType(tf));
            }
        }
        for(StrategySignature signature : index.internalStrategies) {
            final LinkedHashSet<ModuleIdentifier> leftOverModules =
                Relation.remove(strategyIndex, signature, moduleIdentifier);
            if(leftOverModules == null || leftOverModules.isEmpty()) {
                internalStrategies.remove(signature);
            }
        }
        for(StrategyFrontData sfd : index.externalStrategies) {
            final LinkedHashSet<ModuleIdentifier> leftOverModules =
                Relation.remove(strategyIndex, sfd.signature, moduleIdentifier);
            externalStrategyTypes.remove(sfd.signature);
            if(leftOverModules != null && !leftOverModules.isEmpty()) {
                externalStrategyTypes.put(sfd.signature, sfd.signature.standardType(tf));
            }
        }
        for(Map.Entry<StrategySignature, TreeSet<StrategySignature>> e : index.dynamicRules.entrySet()) {
            Relation.remove(strategyIndex, e.getKey(), moduleIdentifier);
            Relation.removeAll(dynamicRules, e.getKey(), e.getValue());
        }
        for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : index.overlayData
            .entrySet()) {
            Relation.remove(overlayIndex, e.getKey(), moduleIdentifier);
            // FIXME: potentially unsound when there are duplicates?
            overlayData.removeAll(e.getValue());
        }
        for(Map.Entry<ConstructorSignature, ArrayList<IStrategoTerm>> e : index.overlayAsts
            .entrySet()) {
            Relation.remove(overlayIndex, e.getKey(), moduleIdentifier);
            Relation.removeAll(overlayAsts, e.getKey(), e.getValue());
        }

        messages.removeAll(index.messages);

        // additions
        index = diff.additions;

        importedStr2LibPackageNames.addAll(index.str2LibPackageNames);
        nonExternalSorts.addAll(index.sorts);
        externalSorts.addAll(index.externalSorts);
        nonExternalConstructors.addAll(index.nonOverlayConstructors);
        externalConstructors.addAll(index.externalConstructors);
        Relation.putAll(nonExternalInjections, index.injections, ArrayList::new);
        for(StrategyFrontData sfd : index.strategies) {
            Relation.getOrInitialize(strategyIndex, sfd.signature, LinkedHashSet::new)
                .add(moduleIdentifier);
            final @Nullable StrategyType current = strategyTypes.get(sfd.signature);
            if(current == null || current instanceof StrategyType.Standard
                && sfd.kind.equals(StrategyFrontData.Kind.TypeDefinition)) {
                strategyTypes.put(sfd.signature, sfd.type);
                continue;
            }
            if(!(current instanceof StrategyType.Standard)
                && sfd.kind.equals(StrategyFrontData.Kind.TypeDefinition)) {
                messages.add(new DuplicateTypeDefinition(current, lastModified));
                messages.add(new DuplicateTypeDefinition(sfd.signature.getSubterm(0), lastModified));
            }
        }
        for(StrategySignature signature : index.internalStrategies) {
            Relation.getOrInitialize(strategyIndex, signature, LinkedHashSet::new)
                .add(moduleIdentifier);
            internalStrategies.add(signature);
        }
        for(StrategyFrontData sfd : index.externalStrategies) {
            Relation.getOrInitialize(strategyIndex, sfd.signature, LinkedHashSet::new)
                .add(moduleIdentifier);
            final @Nullable StrategyType current = externalStrategyTypes.get(sfd.signature);
            if(current == null || current instanceof StrategyType.Standard
                && sfd.kind.equals(StrategyFrontData.Kind.TypeDefinition)) {
                externalStrategyTypes.put(sfd.signature, sfd.type);
            }
        }
        for(Map.Entry<StrategySignature, TreeSet<StrategySignature>> e : index.dynamicRules.entrySet()) {
            Relation.getOrInitialize(strategyIndex, e.getKey(), LinkedHashSet::new)
                .add(moduleIdentifier);
            Relation.getOrInitialize(dynamicRules, e.getKey(), TreeSet::new).addAll(e.getValue());
        }
        for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : index.overlayData
            .entrySet()) {
            Relation.getOrInitialize(overlayIndex, e.getKey(), LinkedHashSet::new)
                .add(moduleIdentifier);
            overlayData.addAll(e.getValue());
        }
        for(Map.Entry<ConstructorSignature, ArrayList<IStrategoTerm>> e : index.overlayAsts
            .entrySet()) {
            Relation.getOrInitialize(overlayIndex, e.getKey(), LinkedHashSet::new)
                .add(moduleIdentifier);
            Relation.getOrInitialize(overlayAsts, e.getKey(), ArrayList::new)
                .addAll(e.getValue());
        }

        messages.addAll(index.messages);

        return new GlobalData(allModuleIdentifiers, importedStr2LibPackageNames, overlayIndex,
            nonExternalInjections, strategyIndex, strategyTypes, nonExternalSorts, externalSorts,
            nonExternalConstructors, externalConstructors, internalStrategies, externalStrategyTypes,
            dynamicRules, overlayData, overlayAsts, messages, lastModified);
    }

    static FrontInput getFrontInput(ResolveInput input,
        IModuleImportService.ModuleIdentifier moduleIdentifier) {
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
