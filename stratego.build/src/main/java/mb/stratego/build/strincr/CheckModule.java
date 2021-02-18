package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import io.usethesource.capsule.BinaryRelation;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.Message2;
import mb.stratego.build.strincr.message.java.StrategyOverlapsWithDynamicRuleHelper;
import mb.stratego.build.strincr.message.stratego.DuplicateTypeDefinition;
import mb.stratego.build.strincr.message.stratego.MissingDefinitionForTypeDefinition;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;

public class CheckModule implements TaskDef<CheckModule.Input, CheckModule.Output> {
    public static final String id = CheckModule.class.getCanonicalName();

    public static class Input extends Front.Input {
        public final ModuleIdentifier mainModuleIdentifier;

        public Input(ModuleIdentifier mainModuleIdentifier, ModuleIdentifier moduleIdentifier,
            IModuleImportService moduleImportService) {
            super(moduleIdentifier, moduleImportService);
            this.mainModuleIdentifier = mainModuleIdentifier;
        }

        public Check.Input resolveInput() {
            return new Check.Input(mainModuleIdentifier, moduleImportService);
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            Input input = (Input) o;

            return mainModuleIdentifier.equals(input.mainModuleIdentifier);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + mainModuleIdentifier.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CheckModule.Input(" + moduleIdentifier + ")";
        }
    }

    public static class Output implements Serializable {
        public final Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts;
        public final Map<StrategySignature, Set<StrategySignature>> dynamicRules;
        public final List<Message2<?>> messages;

        public Output(Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts,
            Map<StrategySignature, Set<StrategySignature>> dynamicRules,
            List<Message2<?>> messages) {
            this.strategyDataWithCasts = strategyDataWithCasts;
            this.dynamicRules = dynamicRules;
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Output output = (Output) o;

            if(!strategyDataWithCasts.equals(output.strategyDataWithCasts))
                return false;
            if(!dynamicRules.equals(output.dynamicRules))
                return false;
            return messages.equals(output.messages);
        }

        @Override public int hashCode() {
            int result = strategyDataWithCasts.hashCode();
            result = 31 * result + dynamicRules.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CheckModule.Output(" + messages.size() + ")";
        }

    }

    private final Resolve resolve;
    private final Front front;
    private final Lib lib;
    private final InsertCasts insertCasts;
    private final ITermFactory tf;

    @Inject public CheckModule(Resolve resolve, Front front, Lib lib, InsertCasts insertCasts,
        StrIncrContext strIncrContext) {
        this.resolve = resolve;
        this.front = front;
        this.lib = lib;
        this.insertCasts = insertCasts;
        this.tf = strIncrContext.getFactory();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final @Nullable ModuleData moduleData = context.require(front, input);
        assert moduleData != null;

        final GTEnvironment environment = prepareGTEnvironment(context, input, moduleData);
        final InsertCasts.Input2 input2 =
            new InsertCasts.Input2(input.moduleIdentifier, environment);
        final @Nullable InsertCasts.Output output = context.require(insertCasts, input2);
        assert output != null;

        final Map<StrategySignature, Set<StrategySignature>> dynamicRules = new HashMap<>();
        final Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts =
            extractStrategyDefs(input.moduleIdentifier, input2.environment.lastModified,
                output.astWithCasts, dynamicRules);

        final List<Message2<?>> messages = new ArrayList<>(output.messages.size());
        for(Message<?> message : output.messages) {
            messages.add(Message2.from(message));
        }

        checkExternalsInternalsOverlap(context, input, moduleData.normalStrategyData,
            moduleData.dynamicRuleData.keySet(), messages);

        return new Output(strategyDataWithCasts, dynamicRules, messages);
    }

    private void checkExternalsInternalsOverlap(ExecContext context, Input input,
        Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData,
        Set<StrategySignature> dynamicRuleGenerated,
        List<Message2<?>> messages) {
        final HashSet<StrategySignature> strategyFilter =
            new HashSet<>(normalStrategyData.keySet());
        strategyFilter.addAll(dynamicRuleGenerated);
        final AnnoDefs annoDefs = PieUtils.requirePartial(context, resolve, input.resolveInput(),
            new GlobalData.ToAnnoDefs(strategyFilter));

        for(Map.Entry<StrategySignature, Set<StrategyFrontData>> e : normalStrategyData
            .entrySet()) {
            final StrategySignature strategySignature = e.getKey();
            final IStrategoString signatureNameTerm = TermUtils.toStringAt(strategySignature, 0);
            final EnumSet<StrategyFrontData.Kind> kinds =
                EnumSet.noneOf(StrategyFrontData.Kind.class);
            final String moduleString = input.moduleIdentifier.moduleString();
            for(StrategyFrontData strategyFrontData : e.getValue()) {
                if(strategyFrontData.kind == StrategyFrontData.Kind.TypeDefinition && kinds
                    .contains(strategyFrontData.kind)) {
                    messages.add(Message2.from(new DuplicateTypeDefinition(moduleString,
                        strategyFrontData.signature.getSubterm(0), MessageSeverity.ERROR)));
                }
                kinds.add(strategyFrontData.kind);
            }
            if(kinds.contains(StrategyFrontData.Kind.Override) || kinds
                .contains(StrategyFrontData.Kind.Extend)) {
                if(!annoDefs.externalStrategySigs.contains(strategySignature)) {
                    messages.add(Message2
                        .from(Message.externalStrategyNotFound(moduleString, signatureNameTerm)));
                }
            }
            if(kinds.contains(StrategyFrontData.Kind.Normal)) {
                if(annoDefs.externalStrategySigs.contains(strategySignature)) {
                    messages.add(Message2
                        .from(Message.externalStrategyOverlap(moduleString, signatureNameTerm)));
                }
                if(annoDefs.internalStrategySigs.contains(strategySignature)) {
                    messages.add(Message2
                        .from(Message.internalStrategyOverlap(moduleString, signatureNameTerm)));
                }
                if(dynamicRuleGenerated.contains(strategySignature)) {
                    messages.add(Message2.from(
                        new StrategyOverlapsWithDynamicRuleHelper(input.moduleIdentifier,
                            signatureNameTerm, strategySignature, MessageSeverity.ERROR)));
                }
            } else {
                if(kinds.contains(StrategyFrontData.Kind.TypeDefinition)) {
                    messages.add(Message2.from(
                        new MissingDefinitionForTypeDefinition(moduleString, signatureNameTerm,
                            MessageSeverity.ERROR)));
                }
            }
        }
    }

    public static Map<StrategySignature, Set<StrategyAnalysisData>> extractStrategyDefs(
        ModuleIdentifier moduleIdentifier, long lastModified, IStrategoTerm ast,
        Map<StrategySignature, Set<StrategySignature>> dynamicRules) throws WrongASTException {
        final Map<StrategySignature, Set<StrategyAnalysisData>> strategyData = new HashMap<>();

        final IStrategoList defs = Front.getDefs(moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new WrongASTException(moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                case "Overlays":
                case "Signature":
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    addStrategyData(moduleIdentifier, lastModified, strategyData, def.getSubterm(0),
                        dynamicRules);
                    break;
                default:
                    throw new WrongASTException(moduleIdentifier, def);
            }
        }
        return strategyData;
    }

    private static void addStrategyData(ModuleIdentifier moduleIdentifier, long lastModified,
        Map<StrategySignature, Set<StrategyAnalysisData>> strategyData, IStrategoTerm strategyDefs,
        Map<StrategySignature, Set<StrategySignature>> dynamicRules) throws WrongASTException {
        for(IStrategoTerm strategyDef : strategyDefs) {
            if(!TermUtils.isAppl(strategyDef, "DefHasType", 3)) {
                if(TermUtils.isAppl(strategyDef, "AnnoDef", 2)) {
                    strategyDef = strategyDef.getSubterm(1);
                }
                if(!TermUtils.isAppl(strategyDef)) {
                    throw new WrongASTException(moduleIdentifier, strategyDef);
                }
                final IStrategoAppl strategyDefAppl = TermUtils.toAppl(strategyDef);
                if(!TermUtils.isStringAt(strategyDefAppl, 0)) {
                    throw new WrongASTException(moduleIdentifier, strategyDefAppl);
                }
                final @Nullable StrategySignature strategySignature =
                    StrategySignature.fromDefinition(strategyDefAppl);
                if(strategySignature == null) {
                    throw new WrongASTException(moduleIdentifier, strategyDefAppl);
                }
                Relation.getOrInitialize(strategyData, strategySignature, HashSet::new)
                    .add(new StrategyAnalysisData(strategyDefAppl, lastModified));
                final Set<StrategySignature> collect = CollectDynRuleSigs.collect(strategyDefAppl);
                for(StrategySignature dynRuleSig : collect) {
                    Relation.getOrInitialize(dynamicRules, dynRuleSig, HashSet::new)
                        .add(strategySignature);
                }
            }
        }
    }

    private GTEnvironment prepareGTEnvironment(ExecContext context, Input input,
        ModuleData moduleData) throws IOException, ExecException {
        final io.usethesource.capsule.Map.Transient<StrategySignature, StrategyType> strategyTypes =
            io.usethesource.capsule.Map.Transient.of();
        final BinaryRelation.Transient<ConstructorSignature, ConstructorType> constructorTypes =
            BinaryRelation.Transient.of();
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections =
            BinaryRelation.Transient.of();

        // Get the relevant strategy and constructor types and all injections, that are defined in
        //     the module itself
        registerModuleDefinitions(moduleData, strategyTypes, constructorTypes, injections);

        // Get the relevant strategy and constructor types and all injections, that are visible
        //     through the import graph
        final java.util.Set<ModuleIdentifier> seen = new HashSet<>();
        final Deque<ModuleIdentifier> workList = new ArrayDeque<>(Resolve
            .expandImports(context, input.moduleImportService, moduleData.imports,
                moduleData.lastModified, null));
        seen.add(input.moduleIdentifier);
        seen.addAll(workList);
        do {
            final ModuleIdentifier moduleIdentifier = workList.remove();

            final Task<ModuleData> task =
                moduleIdentifierToTask(moduleIdentifier, input.moduleImportService);
            final TypesLookup typesLookup = PieUtils.requirePartial(context, task,
                new ModuleData.ToTypesLookup(tf, moduleData.usedStrategies,
                    moduleData.usedAmbiguousStrategies, moduleData.usedConstructors));
            for(Map.Entry<StrategySignature, StrategyType> e : typesLookup.strategyTypes
                .entrySet()) {
                ModuleData.ToTypesLookup
                    .registerStrategyType(strategyTypes, e.getKey(), e.getValue());
            }
            for(Map.Entry<ConstructorSignature, Set<ConstructorType>> e : typesLookup.constructorTypes
                .entrySet()) {
                for(ConstructorType ty : e.getValue()) {
                    constructorTypes.__put(e.getKey(), ty);
                }
            }
            for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : typesLookup.injections
                .entrySet()) {
                for(IStrategoTerm to : e.getValue()) {
                    injections.__put(e.getKey(), to);
                }
            }

            final Set<ModuleIdentifier> expandedImports = Resolve
                .expandImports(context, input.moduleImportService, typesLookup.imports,
                    typesLookup.lastModified, null);
            expandedImports.removeAll(seen);
            workList.addAll(expandedImports);
            seen.addAll(expandedImports);
        } while(!workList.isEmpty());

        final StrategoImmutableMap strategyEnvironment =
            new StrategoImmutableMap(strategyTypes.freeze());
        return GTEnvironment
            .from(strategyEnvironment, constructorTypes.freeze(), injections.freeze(),
                moduleData.ast, tf, moduleData.lastModified);
    }

    private void registerModuleDefinitions(ModuleData moduleData,
        io.usethesource.capsule.Map.Transient<StrategySignature, StrategyType> strategyTypes,
        BinaryRelation.Transient<ConstructorSignature, ConstructorType> constructorTypes,
        BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections) {
        for(Set<StrategyFrontData> strategyFrontData : moduleData.normalStrategyData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ModuleData.ToTypesLookup
                    .registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                        strategyFrontDatum.getType(tf));
            }
        }
        for(Set<StrategyFrontData> strategyFrontData : moduleData.internalStrategyData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ModuleData.ToTypesLookup
                    .registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                        strategyFrontDatum.getType(tf));
            }
        }
        for(Set<StrategyFrontData> strategyFrontData : moduleData.externalStrategyData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ModuleData.ToTypesLookup
                    .registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                        strategyFrontDatum.getType(tf));
            }
        }
        for(Set<StrategyFrontData> strategyFrontData : moduleData.dynamicRuleData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ModuleData.ToTypesLookup
                    .registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                        strategyFrontDatum.getType(tf));
            }
        }
        for(Map.Entry<ConstructorSignature, List<ConstructorData>> e : moduleData.constrData
            .entrySet()) {
            for(ConstructorData d : e.getValue()) {
                constructorTypes.__put(e.getKey(), d.type);
            }
        }
        for(Map.Entry<ConstructorSignature, List<OverlayData>> e : moduleData.overlayData
            .entrySet()) {
            for(OverlayData d : e.getValue()) {
                constructorTypes.__put(e.getKey(), d.type);
            }
        }
        for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : moduleData.injections.entrySet()) {
            for(IStrategoTerm to : e.getValue()) {
                injections.__put(e.getKey(), to);
            }
        }
    }

    private Task<ModuleData> moduleIdentifierToTask(ModuleIdentifier moduleIdentifier,
        IModuleImportService moduleImportService) {
        if(moduleIdentifier.isLibrary()) {
            return lib.createTask(new Front.Input(moduleIdentifier, moduleImportService));
        } else {
            return front.createTask(new Front.Input(moduleIdentifier, moduleImportService));
        }
    }

    @Override public String getId() {
        return id;
    }

    public static class GetStrategyAnalysisData<T extends Set<StrategyAnalysisData> & Serializable>
        implements Function<Output, T>, Serializable {
        public final StrategySignature strategySignature;

        public GetStrategyAnalysisData(StrategySignature strategySignature) {
            this.strategySignature = strategySignature;
        }

        @SuppressWarnings("unchecked") @Override public T apply(Output output) {
            return (T) output.strategyDataWithCasts
                .getOrDefault(strategySignature, Collections.emptySet());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            GetStrategyAnalysisData<?> that = (GetStrategyAnalysisData<?>) o;

            return strategySignature.equals(that.strategySignature);
        }

        @Override public int hashCode() {
            return strategySignature.hashCode();
        }
    }

    public static class GetDynamicRuleAnalysisData<T extends Set<StrategyAnalysisData> & Serializable>
        implements Function<Output, T>, Serializable {
        public final StrategySignature strategySignature;

        public GetDynamicRuleAnalysisData(StrategySignature strategySignature) {
            this.strategySignature = strategySignature;
        }

        @SuppressWarnings("unchecked") @Override public T apply(Output output) {
            final T result = (T) new HashSet<StrategyAnalysisData>();
            for(StrategySignature signature : output.dynamicRules
                .getOrDefault(strategySignature, Collections.emptySet())) {
                final @Nullable Set<StrategyAnalysisData> analysisData =
                    output.strategyDataWithCasts.get(signature);
                if(analysisData != null) {
                    result.addAll(analysisData);
                }
            }
            return result;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            GetDynamicRuleAnalysisData<?> that = (GetDynamicRuleAnalysisData<?>) o;

            return strategySignature.equals(that.strategySignature);
        }

        @Override public int hashCode() {
            return strategySignature.hashCode();
        }
    }
}
