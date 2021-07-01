package mb.stratego.build.strincr.task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;

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
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.StrategoLanguage;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.GTEnvironment;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.function.GetDynamicRuleDefinitions;
import mb.stratego.build.strincr.function.ModulesDefiningStrategy;
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
import mb.stratego.build.strincr.message.TypeSystemInternalCompilerError;
import mb.stratego.build.strincr.message.type.DuplicateTypeDefinition;
import mb.stratego.build.strincr.message.type.MissingDefinitionForTypeDefinition;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.input.ResolveInput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.termvisitors.CountErrT;
import mb.stratego.build.util.InsertCastsInput;
import mb.stratego.build.util.InsertCastsOutput;
import mb.stratego.build.util.InvalidASTException;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;

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

    private final StrategoLanguage strategoLanguage;
    private final ITermFactory tf;
    private final ResourcePathConverter resourcePathConverter;

    @Inject public CheckModule(Resolve resolve, Front front, StrIncrContext strIncrContext,
        StrategoLanguage strategoLanguage, ResourcePathConverter resourcePathConverter) {
        this.resolve = resolve;
        this.front = front;
        this.tf = strIncrContext.getFactory();
        this.strategoLanguage = strategoLanguage;
        this.resourcePathConverter = resourcePathConverter;
    }

    @Override public CheckModuleOutput exec(ExecContext context, CheckModuleInput input) throws Exception {
        if(input.frontInput.moduleIdentifier.isLibrary()) {
            return new CheckModuleOutput(new LinkedHashMap<>(0), new LinkedHashMap<>(0), new ArrayList<>(0));
        }

        final @Nullable ModuleData moduleData = context.require(front, input.frontInput);
        assert moduleData != null;

        final IModuleImportService.ModuleIdentifier moduleIdentifier =
            input.frontInput.moduleIdentifier;

        final GTEnvironment environment =
            prepareGTEnvironment(context, moduleData, input.frontInput);
        final InsertCastsInput insertCastsInput =
            new InsertCastsInput(moduleIdentifier, input.projectPath, environment);
        final String projectPath = resourcePathConverter.toString(input.projectPath);
        final InsertCastsOutput output = insertCasts(insertCastsInput, projectPath);

        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>>
            strategyDataWithCasts =
            extractStrategyDefs(moduleIdentifier, output.astWithCasts, dynamicRules);

        final ArrayList<Message> messages = new ArrayList<>(output.messages.size());
        messages.addAll(output.messages);

        otherChecks(context, input.resolveInput(), moduleData, messages, projectPath);

        return new CheckModuleOutput(strategyDataWithCasts, dynamicRules, messages);
    }

    void otherChecks(ExecContext context, ResolveInput input, ModuleData moduleData,
        ArrayList<Message> messages, String projectPath) throws ExecException {
        checkExternalsInternalsOverlap(context, moduleData.normalStrategyData,
            moduleData.dynamicRuleData.keySet(), moduleData.lastModified, messages, input);
        checkDynamicRuleOverlap(context, input, moduleData.dynamicRules, moduleData.lastModified,
            messages, projectPath);
    }

    private void checkDynamicRuleOverlap(ExecContext context, ResolveInput input,
        LinkedHashSet<StrategySignature> dynamicRules, long lastModified,
        ArrayList<Message> messages, String projectPath) throws ExecException {
        final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningDynamicRule = new HashSet<>();
        for(StrategySignature dynamicRule : dynamicRules) {
            modulesDefiningDynamicRule.addAll(PieUtils.requirePartial(context, resolve, input, new ModulesDefiningStrategy(dynamicRule)));
        }
        final ArrayList<IStrategoTerm> containsDynRuleDefs = new ArrayList<>();
        for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningDynamicRule) {
            containsDynRuleDefs.addAll(PieUtils
                .requirePartial(context, front, Resolve.getFrontInput(input, moduleIdentifier),
                    GetDynamicRuleDefinitions.INSTANCE));
        }
        final IStrategoTerm overlapMessages =
            strategoLanguage.overlapCheck(tf.makeList(containsDynRuleDefs), projectPath);
        for(IStrategoTerm messageTerm : TermUtils.toList(overlapMessages)) {
            messages.add(Message.from(messageTerm, MessageSeverity.ERROR, lastModified));
        }
    }

    InsertCastsOutput insertCasts(InsertCastsInput input, String projectPath) throws ExecException {
        final String moduleName = input.moduleIdentifier.moduleString();

        final IStrategoTerm result =
            strategoLanguage.insertCasts(moduleName, input.environment, projectPath);

        final IStrategoTerm astWithCasts = result.getSubterm(0);
        final IStrategoList errors = TermUtils.toListAt(result, 1);
        final IStrategoList warnings = TermUtils.toListAt(result, 2);
        final IStrategoList notes = TermUtils.toListAt(result, 3);

        final long lastModified = input.environment.lastModified;
        ArrayList<Message> messages =
            new ArrayList<>(errors.size() + warnings.size() + notes.size());
        for(IStrategoTerm errorTerm : errors) {
            messages.add(Message.from(errorTerm, MessageSeverity.ERROR, lastModified));
        }
        for(IStrategoTerm warningTerm : warnings) {
            messages.add(Message.from(warningTerm, MessageSeverity.WARNING, lastModified));
        }
        for(IStrategoTerm noteTerm : notes) {
            messages.add(Message.from(noteTerm, MessageSeverity.NOTE, lastModified));
        }

        // sanity check
        if(errors.isEmpty()) {
            if(CountErrT.countErrT(astWithCasts) > 0) {
                messages.add(
                    new TypeSystemInternalCompilerError(input.environment.ast, MessageSeverity.ERROR,
                        lastModified));
            }
        }
        return new InsertCastsOutput(astWithCasts, messages);
    }

    private void checkExternalsInternalsOverlap(ExecContext context,
        Map<StrategySignature, LinkedHashSet<StrategyFrontData>> normalStrategyData,
        Collection<StrategySignature> dynamicRuleGenerated, long lastModified,
        ArrayList<Message> messages, ResolveInput resolveInput) {
        final HashSet<StrategySignature> strategyFilter =
            new HashSet<>(normalStrategyData.keySet());
        strategyFilter.addAll(dynamicRuleGenerated);
        final AnnoDefs annoDefs =
            PieUtils.requirePartial(context, resolve, resolveInput, new ToAnnoDefs(strategyFilter));

        for(Map.Entry<StrategySignature, LinkedHashSet<StrategyFrontData>> e : normalStrategyData
            .entrySet()) {
            final StrategySignature strategySignature = e.getKey();
            final IStrategoString signatureNameTerm = TermUtils.toStringAt(strategySignature, 0);
            final EnumSet<StrategyFrontData.Kind> kinds =
                EnumSet.noneOf(StrategyFrontData.Kind.class);
            for(StrategyFrontData strategyFrontData : e.getValue()) {
                if(strategyFrontData.kind == StrategyFrontData.Kind.TypeDefinition && kinds
                    .contains(strategyFrontData.kind)) {
                    messages
                        .add(new DuplicateTypeDefinition(strategyFrontData.signature.getSubterm(0),
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
                if(kinds.contains(StrategyFrontData.Kind.TypeDefinition) && !kinds
                    .contains(StrategyFrontData.Kind.External)) {
                    messages.add(new MissingDefinitionForTypeDefinition(signatureNameTerm,
                        MessageSeverity.ERROR, lastModified));
                }
            }
        }
    }

    public static LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>> extractStrategyDefs(
        IModuleImportService.ModuleIdentifier moduleIdentifier, IStrategoTerm ast,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules) {
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>> strategyData =
            new LinkedHashMap<>();

        final IStrategoList defs = Front.getDefs(moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new InvalidASTException(moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                case "Overlays":
                case "Signature":
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    addStrategyData(moduleIdentifier, strategyData, def.getSubterm(0),
                        dynamicRules);
                    break;
                default:
                    throw new InvalidASTException(moduleIdentifier, def);
            }
        }
        return strategyData;
    }

    private static void addStrategyData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>> strategyData,
        IStrategoTerm strategyDefs,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules) {
        for(IStrategoTerm strategyDef : strategyDefs) {
            if(!TermUtils.isAppl(strategyDef, "DefHasType", 2)) {
                if(TermUtils.isAppl(strategyDef, "AnnoDef", 2)) {
                    strategyDef = strategyDef.getSubterm(1);
                }
                if(!TermUtils.isAppl(strategyDef)) {
                    throw new InvalidASTException(moduleIdentifier, strategyDef);
                }
                final IStrategoAppl strategyDefAppl = TermUtils.toAppl(strategyDef);
                if(!TermUtils.isStringAt(strategyDefAppl, 0)) {
                    throw new InvalidASTException(moduleIdentifier, strategyDefAppl);
                }
                final @Nullable StrategySignature strategySignature =
                    StrategySignature.fromDefinition(strategyDefAppl);
                if(strategySignature == null) {
                    throw new InvalidASTException(moduleIdentifier, strategyDefAppl);
                }
                final LinkedHashSet<StrategySignature> definedDynamicRules =
                    CollectDynRuleSigs.collect(strategyDefAppl);
                Relation.getOrInitialize(strategyData, strategySignature, LinkedHashSet::new).add(
                    new StrategyAnalysisData(strategySignature, strategyDefAppl,
                        definedDynamicRules));
                for(StrategySignature dynRuleSig : definedDynamicRules) {
                    Relation.getOrInitialize(dynamicRules, dynRuleSig, LinkedHashSet::new)
                        .add(strategySignature);
                }
            }
        }
    }

    GTEnvironment prepareGTEnvironment(ExecContext context, ModuleData moduleData,
        FrontInput frontInput) {
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
        //     through the import, not following them transitively!
        final ToTypesLookup toTypesLookup =
            new ToTypesLookup(moduleData.usedStrategies, moduleData.usedAmbiguousStrategies,
                moduleData.usedConstructors);
        final HashSet<IModuleImportService.ModuleIdentifier> seen = new HashSet<>();
        seen.add(frontInput.moduleIdentifier);
        seen.addAll(moduleData.imports);
        final Queue<IModuleImportService.ModuleIdentifier> worklist = new ArrayDeque<>(moduleData.imports);
        while(!worklist.isEmpty()) {
            final IModuleImportService.ModuleIdentifier moduleIdentifier = worklist.remove();
            final FrontInput moduleInput =
                new FrontInput.Normal(moduleIdentifier, frontInput.strFileGeneratingTasks,
                    frontInput.includeDirs, frontInput.linkedLibraries, frontInput.autoImportStd);
            final TypesLookup typesLookup =
                PieUtils.requirePartial(context, front, moduleInput, toTypesLookup);
            for(Map.Entry<StrategySignature, StrategyType> e : typesLookup.strategyTypes
                .entrySet()) {
                ToTypesLookup.registerStrategyType(strategyTypes, e.getKey(), e.getValue());
            }
            for(Map.Entry<ConstructorSignature, HashSet<ConstructorType>> e : typesLookup.constructorTypes
                .entrySet()) {
                for(ConstructorType ty : e.getValue()) {
                    constructorTypes.__insert(e.getKey(), ty);
                }
            }
            for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : typesLookup.allInjections
                .entrySet()) {
                for(IStrategoTerm to : e.getValue()) {
                    injections.__insert(e.getKey(), to);
                }
            }
            if(frontInput.moduleIdentifier.legacyStratego()) {
                for(IModuleImportService.ModuleIdentifier anImport : typesLookup.imports) {
                    if(!seen.contains(anImport)) {
                        seen.add(anImport);
                        worklist.add(anImport);
                    }
                }
            }
        }

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
        for(HashSet<StrategyFrontData> strategyFrontData : moduleData.normalStrategyData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(HashSet<StrategyFrontData> strategyFrontData : moduleData.internalStrategyData
            .values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(HashSet<StrategyFrontData> strategyFrontData : moduleData.externalStrategyData
            .values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(HashSet<StrategyFrontData> strategyFrontData : moduleData.dynamicRuleData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : moduleData.constrData
            .entrySet()) {
            for(ConstructorData d : e.getValue()) {
                constructorTypes.__put(e.getKey(), d.type);
            }
        }
        for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : moduleData.externalConstrData
            .entrySet()) {
            for(ConstructorData d : e.getValue()) {
                constructorTypes.__put(e.getKey(), d.type);
            }
        }
        for(Map.Entry<ConstructorSignature, ArrayList<OverlayData>> e : moduleData.overlayData
            .entrySet()) {
            for(OverlayData d : e.getValue()) {
                constructorTypes.__put(e.getKey(), d.type);
            }
        }
        for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : moduleData.injections
            .entrySet()) {
            for(IStrategoTerm to : e.getValue()) {
                injections.__put(e.getKey(), to);
            }
        }
        for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : moduleData.externalInjections
            .entrySet()) {
            for(IStrategoTerm to : e.getValue()) {
                injections.__put(e.getKey(), to);
            }
        }
    }

    @Override public String getId() {
        return id;
    }

}
