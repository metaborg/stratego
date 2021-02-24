package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.strc.insert_casts_0_0;

import io.usethesource.capsule.BinaryRelation;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.GTEnvironment;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.function.ToAnnoDefs;
import mb.stratego.build.strincr.function.ToTypesLookup;
import mb.stratego.build.strincr.function.output.AnnoDefs;
import mb.stratego.build.strincr.function.output.TypesLookup;
import mb.stratego.build.strincr.message.ExternalStrategyNotFound;
import mb.stratego.build.strincr.message.ExternalStrategyOverlap;
import mb.stratego.build.strincr.message.InternalStrategyOverlap;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategyOverlapsWithDynamicRuleHelper;
import mb.stratego.build.strincr.message.type.DuplicateTypeDefinition;
import mb.stratego.build.strincr.message.type.MissingDefinitionForTypeDefinition;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.input.InsertCastsInput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.task.output.InsertCastsOutput;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.build.util.WrongASTException;

/**
 * Runs static checks on a module, based on the {@link ModuleData} and that of the modules are
 * visible through imports. This is currently still transitive imports, but we may drop support of
 * that to improve performance of the compiler. But that might influence other meta-languages that
 * generate Stratego code. Static checks are mostly done in Stratego, although some overlap checks
 * were easier to keep in Java.
 */
public class CheckModule implements TaskDef<CheckModuleInput, CheckModuleOutput> {
    public static final String id = "stratego." + CheckModule.class.getSimpleName();

    private final Resolve resolve;
    private final Front front;
    private final Lib lib;

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;
    private final ITermFactory tf;
    private final IModuleImportService moduleImportService;

    @Inject public CheckModule(Resolve resolve, Front front, Lib lib, StrIncrContext strIncrContext,
        IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext,
        IModuleImportService moduleImportService) {
        this.resolve = resolve;
        this.front = front;
        this.lib = lib;
        this.tf = strIncrContext.getFactory();
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
        this.moduleImportService = moduleImportService;
    }

    @Override public CheckModuleOutput exec(ExecContext context, CheckModuleInput input) throws Exception {
        final @Nullable ModuleData moduleData = context.require(front, input.frontInput());
        assert moduleData != null;

        final GTEnvironment environment = prepareGTEnvironment(context, input, moduleData);
        final InsertCastsInput insertCastsInput =
            new InsertCastsInput(input.moduleIdentifier(), environment);
        final InsertCastsOutput output = insertCasts(context, insertCastsInput);

        final Map<StrategySignature, Set<StrategySignature>> dynamicRules = new HashMap<>();
        final Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts =
            extractStrategyDefs(input.moduleIdentifier(), moduleData.lastModified,
                output.astWithCasts, dynamicRules);

        final List<Message<?>> messages = new ArrayList<>(output.messages.size());
        messages.addAll(output.messages);

        checkExternalsInternalsOverlap(context, input, moduleData.normalStrategyData,
            moduleData.dynamicRuleData.keySet(), moduleData.lastModified, messages);

        return new CheckModuleOutput(strategyDataWithCasts, dynamicRules, messages);
    }

    private InsertCastsOutput insertCasts(ExecContext execContext, InsertCastsInput input)
        throws ExecException {
        final String moduleName = input.moduleIdentifier.moduleString();

        final StrategoExecutor.ExecutionResult output = StrategoExecutor
            .runLocallyUniqueStringStrategy(ioAgentTrackerFactory, execContext.logger(), true,
                insert_casts_0_0.instance, input.environment, strContext);
        if(!output.success) {
            throw new ExecException(
                "Call to insert_casts failed on " + moduleName + ": \n" + output.exception);
        }
        assert output.result != null;

        final IStrategoTerm astWithCasts = output.result.getSubterm(0);
        final IStrategoList errors = TermUtils.toListAt(output.result, 1);
        final IStrategoList warnings = TermUtils.toListAt(output.result, 2);
        final IStrategoList notes = TermUtils.toListAt(output.result, 3);

        final long lastModified = input.environment.lastModified;
        List<Message<?>> messages = new ArrayList<>(errors.size() + warnings.size() + notes.size());
        for(IStrategoTerm errorTerm : errors) {
            messages.add(
                Message.from(execContext.logger(), errorTerm, MessageSeverity.ERROR, lastModified));
        }
        for(IStrategoTerm warningTerm : warnings) {
            messages.add(Message
                .from(execContext.logger(), warningTerm, MessageSeverity.WARNING, lastModified));
        }
        for(IStrategoTerm noteTerm : notes) {
            messages.add(
                Message.from(execContext.logger(), noteTerm, MessageSeverity.NOTE, lastModified));
        }
        return new InsertCastsOutput(astWithCasts, messages);
    }

    private void checkExternalsInternalsOverlap(ExecContext context, CheckModuleInput input,
        Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData,
        Set<StrategySignature> dynamicRuleGenerated, long lastModified, List<Message<?>> messages) {
        final HashSet<StrategySignature> strategyFilter =
            new HashSet<>(normalStrategyData.keySet());
        strategyFilter.addAll(dynamicRuleGenerated);
        final AnnoDefs annoDefs = PieUtils
            .requirePartial(context, resolve, input.resolveInput(), new ToAnnoDefs(strategyFilter));

        for(Map.Entry<StrategySignature, Set<StrategyFrontData>> e : normalStrategyData
            .entrySet()) {
            final StrategySignature strategySignature = e.getKey();
            final IStrategoString signatureNameTerm = TermUtils.toStringAt(strategySignature, 0);
            final EnumSet<StrategyFrontData.Kind> kinds =
                EnumSet.noneOf(StrategyFrontData.Kind.class);
            for(StrategyFrontData strategyFrontData : e.getValue()) {
                if(strategyFrontData.kind == StrategyFrontData.Kind.TypeDefinition && kinds
                    .contains(strategyFrontData.kind)) {
                    messages.add(
                        new DuplicateTypeDefinition(strategyFrontData.signature.getSubterm(0),
                            MessageSeverity.ERROR, lastModified));
                }
                kinds.add(strategyFrontData.kind);
            }
            if(kinds.contains(StrategyFrontData.Kind.Override) || kinds
                .contains(StrategyFrontData.Kind.Extend)) {
                if(!annoDefs.externalStrategySigs.contains(strategySignature)) {
                    messages.add(
                        ExternalStrategyNotFound.followOrigin(signatureNameTerm, lastModified));
                }
            }
            if(kinds.contains(StrategyFrontData.Kind.Normal)) {
                if(annoDefs.externalStrategySigs.contains(strategySignature)) {
                    messages
                        .add(ExternalStrategyOverlap.followOrigin(signatureNameTerm, lastModified));
                }
                if(annoDefs.internalStrategySigs.contains(strategySignature)) {
                    messages
                        .add(InternalStrategyOverlap.followOrigin(signatureNameTerm, lastModified));
                }
                if(dynamicRuleGenerated.contains(strategySignature)) {
                    messages.add(new StrategyOverlapsWithDynamicRuleHelper(signatureNameTerm,
                        strategySignature, MessageSeverity.ERROR, lastModified));
                }
            } else {
                if(kinds.contains(StrategyFrontData.Kind.TypeDefinition)) {
                    messages.add(new MissingDefinitionForTypeDefinition(signatureNameTerm,
                        MessageSeverity.ERROR, lastModified));
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
                final Set<StrategySignature> definedDynamicRules = CollectDynRuleSigs.collect(strategyDefAppl);
                Relation.getOrInitialize(strategyData, strategySignature, HashSet::new)
                    .add(new StrategyAnalysisData(strategySignature, strategyDefAppl, definedDynamicRules, lastModified));
                for(StrategySignature dynRuleSig : definedDynamicRules) {
                    Relation.getOrInitialize(dynamicRules, dynRuleSig, HashSet::new)
                        .add(strategySignature);
                }
            }
        }
    }

    private GTEnvironment prepareGTEnvironment(ExecContext context, CheckModuleInput input,
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
            .expandImports(context, moduleImportService, moduleData.imports,
                moduleData.lastModified, null, input.strFileGeneratingTasks(), input.includeDirs()));
        seen.add(input.moduleIdentifier());
        seen.addAll(workList);
        do {
            final ModuleIdentifier moduleIdentifier = workList.remove();

            final Task<ModuleData> task =
                moduleIdentifierToTask(moduleIdentifier, input.strFileGeneratingTasks());
            final TypesLookup typesLookup = PieUtils.requirePartial(context, task,
                new ToTypesLookup(tf, moduleData.usedStrategies,
                    moduleData.usedAmbiguousStrategies, moduleData.usedConstructors));
            for(Map.Entry<StrategySignature, StrategyType> e : typesLookup.strategyTypes
                .entrySet()) {
                ToTypesLookup
                    .registerStrategyType(strategyTypes, e.getKey(), e.getValue());
            }
            for(Map.Entry<ConstructorSignature, Set<ConstructorType>> e : typesLookup.constructorTypes
                .entrySet()) {
                for(ConstructorType ty : e.getValue()) {
                    constructorTypes.__put(e.getKey(), ty);
                }
            }
            for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : typesLookup.allInjections
                .entrySet()) {
                for(IStrategoTerm to : e.getValue()) {
                    injections.__put(e.getKey(), to);
                }
            }

            final Set<ModuleIdentifier> expandedImports = Resolve
                .expandImports(context, moduleImportService, typesLookup.imports,
                    typesLookup.lastModified, null, input.strFileGeneratingTasks(), input.includeDirs());
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
                ToTypesLookup
                    .registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                        strategyFrontDatum.getType(tf));
            }
        }
        for(Set<StrategyFrontData> strategyFrontData : moduleData.internalStrategyData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup
                    .registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                        strategyFrontDatum.getType(tf));
            }
        }
        for(Set<StrategyFrontData> strategyFrontData : moduleData.externalStrategyData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup
                    .registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                        strategyFrontDatum.getType(tf));
            }
        }
        for(Set<StrategyFrontData> strategyFrontData : moduleData.dynamicRuleData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup
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
        for(Map.Entry<ConstructorSignature, List<ConstructorData>> e : moduleData.externalConstrData
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
        for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : moduleData.externalInjections.entrySet()) {
            for(IStrategoTerm to : e.getValue()) {
                injections.__put(e.getKey(), to);
            }
        }
    }

    private Task<ModuleData> moduleIdentifierToTask(ModuleIdentifier moduleIdentifier,
        Collection<STask<?>> strFileGeneratingTasks) {
        final FrontInput input = new FrontInput.Normal(moduleIdentifier, strFileGeneratingTasks);
        if(moduleIdentifier.isLibrary()) {
            return lib.createTask(input);
        } else {
            return front.createTask(input);
        }
    }

    @Override public String getId() {
        return id;
    }

}
