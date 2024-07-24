package mb.stratego.build.strincr.task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Set;
import mb.jsglr.shared.ImploderAttachment;
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
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.function.GetAllModuleIdentifiers;
import mb.stratego.build.strincr.function.GetDynamicRuleDefinitions;
import mb.stratego.build.strincr.function.GetStrategyType;
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
import mb.stratego.build.strincr.message.UnreachableModule;
import mb.stratego.build.strincr.message.type.DuplicateTypeDefinition;
import mb.stratego.build.strincr.message.type.MissingDefinitionForTypeDefinition;
import mb.stratego.build.strincr.message.type.MissingStrategyTypeImport;
import mb.stratego.build.strincr.message.type.MissingTypeDefinition;
import mb.stratego.build.strincr.message.type.TypeSystemInternalCompilerError;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.input.ResolveInput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.termvisitors.FindErrT;
import mb.stratego.build.termvisitors.FindSortTP;
import mb.stratego.build.util.InsertCastsInput;
import mb.stratego.build.util.InsertCastsOutput;
import mb.stratego.build.util.InvalidASTException;
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

    @jakarta.inject.Inject public CheckModule(Resolve resolve, Front front, StrIncrContext strIncrContext,
        StrategoLanguage strategoLanguage, ResourcePathConverter resourcePathConverter) {
        this.resolve = resolve;
        this.front = front;
        this.tf = strIncrContext.getFactory();
        this.strategoLanguage = strategoLanguage;
        this.resourcePathConverter = resourcePathConverter;
    }

    @Override public CheckModuleOutput exec(ExecContext context, CheckModuleInput input) throws Exception {
        if(input.frontInput.moduleIdentifier.isLibrary()) {
            return new CheckModuleOutput(new LinkedHashMap<>(0), new LinkedHashMap<>(0),
                new LinkedHashSet<>(0), new ArrayList<>(0));
        }

        final @Nullable ModuleData moduleData = context.require(front, input.frontInput);
        assert moduleData != null;

        final IModuleImportService.ModuleIdentifier moduleIdentifier =
            input.frontInput.moduleIdentifier;
        final ArrayList<Message> messages = new ArrayList<>();

        final LinkedHashSet<StrategySignature> strategiesDefinedByModule = new LinkedHashSet<>();
        final ResolveInput resolveInput = input.resolveInput();
        final GTEnvironment environment =
            prepareGTEnvironment(context, moduleData, input.frontInput, strategiesDefinedByModule,
                resolveInput, messages);
        final InsertCastsInput insertCastsInput =
            new InsertCastsInput(moduleIdentifier, input.projectPath, environment);
        final String projectPath = resourcePathConverter.toString(input.projectPath);
        final InsertCastsOutput output = insertCasts(insertCastsInput, projectPath);

        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>>
            strategyDataWithCasts =
            extractStrategyDefs(moduleIdentifier, output.astWithCasts, dynamicRules);

        messages.addAll(output.messages);

        otherChecks(context, resolveInput, moduleData, messages, projectPath);

        return new CheckModuleOutput(strategyDataWithCasts, dynamicRules, strategiesDefinedByModule,
            messages);
    }

    void otherChecks(ExecContext context, ResolveInput input, ModuleData moduleData,
        ArrayList<Message> messages, String projectPath) throws ExecException {
        final HashMap<StrategySignature, ArrayList<StrategyFrontData>> strategyData = new HashMap<>(moduleData.normalStrategyData);
        Relation.putAll(strategyData, moduleData.internalStrategyData, ArrayList::new);
        Relation.putAll(strategyData, moduleData.externalStrategyData, ArrayList::new);
        checkExternalsInternalsOverlap(context, strategyData,
            moduleData.dynamicRuleData.keySet(), moduleData.lastModified, messages, input);
        checkDynamicRuleOverlap(context, input, moduleData.dynamicRules.keySet(),
            moduleData.lastModified, messages, projectPath);
        checkUnreachableModule(context, input, moduleData, messages);
    }

    private void checkUnreachableModule(ExecContext context, ResolveInput input,
        ModuleData moduleData, ArrayList<Message> messages) {
        LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers =
            context.requireMapping(resolve, input, GetAllModuleIdentifiers.INSTANCE);
        if(!allModuleIdentifiers.contains(moduleData.moduleIdentifier)) {
            messages.add(new UnreachableModule(tryGetModuleName(moduleData.ast), moduleData.moduleIdentifier, moduleData.lastModified));
        }
    }

    private static IStrategoTerm tryGetModuleName(IStrategoTerm ast) {
        if(TermUtils.isAppl(ast, "Module", 2)) {
            return ast.getSubterm(0);
        }
        return ast;
    }

    private void checkDynamicRuleOverlap(ExecContext context, ResolveInput input,
        Collection<StrategySignature> dynamicRules, long lastModified, ArrayList<Message> messages,
        String projectPath) throws ExecException {
        final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningDynamicRule =
            new HashSet<>();
        for(StrategySignature dynamicRule : dynamicRules) {
            modulesDefiningDynamicRule.addAll(
                context.requireMapping(resolve, input, new ModulesDefiningStrategy(dynamicRule)));
        }
        final ArrayList<IStrategoTerm> containsDynRuleDefs = new ArrayList<>();
        for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningDynamicRule) {
            containsDynRuleDefs.addAll(
                context.requireMapping(front, Resolve.getFrontInput(input, moduleIdentifier),
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

        // sanity checks
        final List<StrategySignature> defsWithSortTP = FindSortTP.findSortTP(astWithCasts);
        if(!defsWithSortTP.isEmpty()) {
            messages.add(new TypeSystemInternalCompilerError(astToFilenameTerm(input.environment.ast),
                "type system did not give error message on checking TP but did not prove it was TP either", defsWithSortTP,
                MessageSeverity.ERROR, lastModified));
        }
        if(errors.isEmpty()) {
            final List<StrategySignature> defsWithErrT = FindErrT.findErrT(astWithCasts);
            if(!defsWithErrT.isEmpty()) {
                messages.add(new TypeSystemInternalCompilerError(astToFilenameTerm(input.environment.ast),
                    "type system did not give errors but did insert cast to error type in", defsWithErrT,
                    MessageSeverity.ERROR, lastModified));
            }
        }
        return new InsertCastsOutput(astWithCasts, messages);
    }

    private IStrategoString astToFilenameTerm(IStrategoTerm ast) {
        final @Nullable ImploderAttachment location = ImploderAttachment.get(OriginAttachment.tryGetOrigin(ast));
        final String filename = location.getLeftToken().getFilename();
        final IStrategoString filenameTerm = tf.makeString(filename);
        if(location != null) {
            filenameTerm.putAttachment(ImploderAttachment.createCompactPositionAttachment(filename, 0, 0, 0, 0));
        }
        return filenameTerm;
    }

    private void checkExternalsInternalsOverlap(ExecContext context,
        Map<StrategySignature, ArrayList<StrategyFrontData>> strategyData,
        Collection<StrategySignature> dynamicRuleGenerated, long lastModified,
        ArrayList<Message> messages, ResolveInput resolveInput) {
        final LinkedHashSet<StrategySignature> strategyFilter =
            new LinkedHashSet<>(strategyData.keySet());
        strategyFilter.addAll(dynamicRuleGenerated);
        final AnnoDefs annoDefs =
            context.requireMapping(resolve, resolveInput, new ToAnnoDefs(strategyFilter));

        for(Map.Entry<StrategySignature, ArrayList<StrategyFrontData>> e : strategyData
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
                            lastModified));
                }
                kinds.add(strategyFrontData.kind);
            }
            if(kinds.contains(StrategyFrontData.Kind.Override) || kinds
                .contains(StrategyFrontData.Kind.Extend)) {
                if(!annoDefs.externalStrategySigs.contains(strategySignature)) {
                    messages.add(ExternalStrategyNotFound.followOrigin(signatureNameTerm,
                        annoDefs.lastModified));
                }
            }
            if(kinds.contains(StrategyFrontData.Kind.Normal)) {
                if(annoDefs.externalStrategySigs.contains(strategySignature)) {
                    messages.add(ExternalStrategyOverlap.followOrigin(signatureNameTerm,
                        annoDefs.lastModified));
                }
                if(annoDefs.internalStrategySigs.contains(strategySignature)) {
                    messages.add(InternalStrategyOverlap.followOrigin(signatureNameTerm,
                        annoDefs.lastModified));
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
                final TreeSet<StrategySignature> definedDynamicRules =
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
        FrontInput frontInput, LinkedHashSet<StrategySignature> moduleDefinitions,
        ResolveInput resolveInput, ArrayList<Message> messages) {
        final io.usethesource.capsule.Map.Transient<StrategySignature, StrategyType> strategyTypes =
            CapsuleUtil.transientMap();
        final BinaryRelation.Transient<ConstructorSignature, ConstructorType> constructorTypes =
            BinaryRelation.Transient.of();
        final Set.Transient<SortSignature> sorts = CapsuleUtil.transientSet();
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections =
            BinaryRelation.Transient.of();

        // Get the relevant strategy and constructor types and all injections, that are defined in
        //     the module itself
        registerModuleDefinitions(moduleData, strategyTypes, constructorTypes, sorts, injections);

        moduleDefinitions.addAll(moduleData.normalStrategyData.keySet());
        // TODO: is this necessary? I don't think multiple internal strategy definitions are allowed anyway
        moduleDefinitions.addAll(moduleData.internalStrategyData.keySet());
        // Get the relevant strategy and constructor types and all sorts and injections, that are visible
        //     through the import, not following them transitively!
        final ToTypesLookup toTypesLookup =
            new ToTypesLookup(new LinkedHashSet<>(moduleDefinitions),
                new LinkedHashSet<>(moduleData.overlayData.keySet()), moduleData.usedStrategies,
                moduleData.usedAmbiguousStrategies, moduleData.usedConstructors);
        final HashMap<StrategySignature, IStrategoTerm> strategyDefsWithType = new HashMap<>();
        final HashSet<StrategySignature> strategyDefsWithoutType = new HashSet<>();
        normalStrategyDataLoop:
        for(Map.Entry<StrategySignature, ArrayList<StrategyFrontData>> e : moduleData.normalStrategyData
            .entrySet()) {
            StrategySignature sig = e.getKey();
            for(StrategyFrontData sfd : e.getValue()) {
                if(sfd.kind.equals(StrategyFrontData.Kind.TypeDefinition)) {
                    strategyDefsWithType.put(sig, sfd.signature.getSubterm(0));
                    continue normalStrategyDataLoop;
                }
            }
            strategyDefsWithoutType.add(sig);
        }
        final HashSet<IModuleImportService.ModuleIdentifier> seen = new HashSet<>();
        seen.add(frontInput.moduleIdentifier);
        seen.addAll(moduleData.imports);
        final Queue<IModuleImportService.ModuleIdentifier> worklist = new ArrayDeque<>(moduleData.imports);
        while(!worklist.isEmpty()) {
            final IModuleImportService.ModuleIdentifier moduleIdentifier = worklist.remove();
            final FrontInput moduleInput =
                new FrontInput.Normal(moduleIdentifier, frontInput.importResolutionInfo,
                    frontInput.autoImportStd);
            final TypesLookup typesLookup =
                context.requireMapping(front, moduleInput, toTypesLookup);
            for(Map.Entry<StrategySignature, StrategyType> e : typesLookup.strategyTypes
                .entrySet()) {
                final StrategySignature signature = e.getKey();
                final StrategyType strategyType = e.getValue();

                final @Nullable StrategyType current = strategyTypes.get(signature);
                if(current == null || !(strategyType instanceof StrategyType.Standard)) {
                    if(current == null || current instanceof StrategyType.Standard) {
                        strategyTypes.__put(signature, strategyType);
                    } else if(!(current instanceof StrategyType.Standard) && !(strategyType instanceof StrategyType.Standard)) {
                        // Problem found and reported already in Resolve
                    }
                }

                moduleDefinitions.remove(signature);
                if(!(strategyType instanceof StrategyType.Standard)) {
                    strategyDefsWithoutType.remove(signature);
                }
            }
            for(Map.Entry<ConstructorSignature, HashSet<ConstructorType>> e : typesLookup.constructorTypes
                .entrySet()) {
                for(ConstructorType ty : e.getValue()) {
                    constructorTypes.__insert(e.getKey(), ty);
                }
            }
            sorts.__insertAll(typesLookup.sorts);
            for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : typesLookup.allInjections
                .entrySet()) {
                for(IStrategoTerm to : e.getValue()) {
                    injections.__insert(e.getKey(), to);
                }
            }
            // if it's a Stratego 1 module, we have to follow the transitive imports...
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
        for(StrategySignature strategyDefWithoutType : strategyDefsWithoutType) {
            final GetStrategyType getStrategyType = new GetStrategyType(strategyDefWithoutType);
            final StrategyType strategyType =
                context.requireMapping(resolve, resolveInput, getStrategyType);
            if(strategyType == null || strategyType instanceof StrategyType.Standard) {
                messages.add(new MissingTypeDefinition(strategyDefWithoutType.getSubterm(0), moduleData.lastModified));
            } else {
                messages.add(new MissingStrategyTypeImport(strategyDefWithoutType.getSubterm(0),
                    moduleData.lastModified));
            }
        }
        return GTEnvironment.from(strategyEnvironment, constructorTypes.freeze(), sorts.freeze(),
            injections.freeze(), moduleData.ast, tf, moduleData.lastModified);
    }

    private void registerModuleDefinitions(ModuleData moduleData,
        io.usethesource.capsule.Map.Transient<StrategySignature, StrategyType> strategyTypes,
        BinaryRelation.Transient<ConstructorSignature, ConstructorType> constructorTypes,
        Set.Transient<SortSignature> sorts, BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections) {
        for(ArrayList<StrategyFrontData> strategyFrontData : moduleData.normalStrategyData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(ArrayList<StrategyFrontData> strategyFrontData : moduleData.internalStrategyData
            .values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(ArrayList<StrategyFrontData> strategyFrontData : moduleData.externalStrategyData
            .values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(ArrayList<StrategyFrontData> strategyFrontData : moduleData.dynamicRuleData.values()) {
            for(StrategyFrontData strategyFrontDatum : strategyFrontData) {
                ToTypesLookup.registerStrategyType(strategyTypes, strategyFrontDatum.signature,
                    strategyFrontDatum.type);
            }
        }
        for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : moduleData.constrData
            .entrySet()) {
            for(ConstructorData d : e.getValue()) {
                constructorTypes.__insert(e.getKey(), d.type);
            }
        }
        for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : moduleData.externalConstrData
            .entrySet()) {
            for(ConstructorData d : e.getValue()) {
                constructorTypes.__insert(e.getKey(), d.type);
            }
        }
        for(Map.Entry<ConstructorSignature, ArrayList<ConstructorData>> e : moduleData.overlayData
            .entrySet()) {
            for(ConstructorData d : e.getValue()) {
                constructorTypes.__insert(e.getKey(), d.type);
            }
        }
        sorts.__insertAll(moduleData.sortData);
        sorts.__insertAll(moduleData.externalSortData);
        for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : moduleData.injections
            .entrySet()) {
            for(IStrategoTerm to : e.getValue()) {
                injections.__insert(e.getKey(), to);
            }
        }
        for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : moduleData.externalInjections
            .entrySet()) {
            for(IStrategoTerm to : e.getValue()) {
                injections.__insert(e.getKey(), to);
            }
        }
    }

    @Override public String getId() {
        return id;
    }

}
