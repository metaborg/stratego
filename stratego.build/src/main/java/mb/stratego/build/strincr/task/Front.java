package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import mb.jsglr.shared.ImploderAttachment;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ImportResolution;
import mb.stratego.build.strincr.IModuleImportService.ImportResolutionInfo;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.StrategoLanguage;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.message.ExternalStrategySourceNotFound;
import mb.stratego.build.strincr.message.FailedToGetModuleAst;
import mb.stratego.build.strincr.message.InvalidASTMessage;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.UnresolvedImport;
import mb.stratego.build.strincr.message.UsingStratego1File;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.termvisitors.CollectDynRuleSigs;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.termvisitors.UsedNamesFront;
import mb.stratego.build.util.GenerateStratego;
import mb.stratego.build.util.InvalidASTException;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;

import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.DynRuleGenerated;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.Extend;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.External;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.Internal;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.Normal;
import static mb.stratego.build.strincr.data.StrategyFrontData.Kind.TypeDefinition;

/**
 * Task that takes a {@link IModuleImportService.ModuleIdentifier} and processes the corresponding AST. The AST is split
 * into {@link ModuleData}, which contains the original AST along with several lists of
 * information required in other task.
 */
public class Front implements TaskDef<FrontInput, ModuleData> {
    public static final String id = "stratego." + Front.class.getSimpleName();
    public final IModuleImportService moduleImportService;
    protected final ITermFactory tf;
    protected final GenerateStratego generateStratego;
    protected final StrategoLanguage strategoLanguage;
    protected final ResourcePathConverter resourcePathConverter;

    @jakarta.inject.Inject public Front(StrIncrContext strContext, IModuleImportService moduleImportService,
        GenerateStratego generateStratego, StrategoLanguage strategoLanguage,
        ResourcePathConverter resourcePathConverter) {
        this.tf = strContext.getFactory();
        this.generateStratego = generateStratego;
        this.moduleImportService = moduleImportService;
        this.strategoLanguage = strategoLanguage;
        this.resourcePathConverter = resourcePathConverter;
    }

    @Override public ModuleData exec(ExecContext context, FrontInput input) throws Exception {
        final ArrayList<IModuleImportService.ModuleIdentifier> imports = new ArrayList<>();
        final LinkedHashSet<SortSignature> sortData = new LinkedHashSet<>(0);
        final LinkedHashSet<SortSignature> externalSortData = new LinkedHashSet<>(0);
        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData =
            new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData =
            new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData =
            new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> strategyData =
            new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>>
            internalStrategyData = new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>>
            externalStrategyData = new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> dynamicRuleData =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules = new LinkedHashMap<>();
        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections =
            new LinkedHashMap<>();
        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections =
            new LinkedHashMap<>();
        final ArrayList<Message> messages = new ArrayList<>();

        final LinkedHashSet<ConstructorSignature> usedConstructors = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> usedStrategies = new LinkedHashSet<>();
        final LinkedHashSet<String> usedAmbiguousStrategies = new LinkedHashSet<>();

        final LastModified<IStrategoTerm> ast;
        try {
            ast = getModuleAst(context, input);
            if(input.moduleIdentifier instanceof mb.stratego.build.strincr.ModuleIdentifier
                && ((mb.stratego.build.strincr.ModuleIdentifier) input.moduleIdentifier).path.getLeafFileExtension()
                    .equals("str")) {
                messages.add(new UsingStratego1File(resourcePathToTerm((
                        (mb.stratego.build.strincr.ModuleIdentifier) input.moduleIdentifier).path),
                    ast.lastModified));
            }
        } catch(ExecException | RuntimeException | InterruptedException e) {
            throw e;
        } catch(Exception e) {
            final IStrategoString module = tf.makeString(input.moduleIdentifier.moduleString());
            final @Nullable String fileName = moduleImportService.fileName(input.moduleIdentifier);
            module.putAttachment(ImploderAttachment.createCompactPositionAttachment(
                fileName != null ? fileName : input.moduleIdentifier.moduleString(), 0, 0, 0, 0));
            messages.add(new FailedToGetModuleAst(module, input.moduleIdentifier, e));

            final ArrayList<String> str2LibPackageNames = new ArrayList<>(0);
            return new ModuleData(input.moduleIdentifier, str2LibPackageNames,
                generateStratego.emptyModuleAst(input.moduleIdentifier), imports, sortData,
                externalSortData, constrData, externalConstrData, injections, externalInjections,
                strategyData, internalStrategyData, externalStrategyData, dynamicRuleData,
                overlayData, overlayAsts, overlayUsedConstrs, usedConstructors, usedStrategies, dynamicRules,
                usedAmbiguousStrategies, messages, 0L);
        }

        final ArrayList<String> str2LibPackageNames = strategoLanguage.extractPackageNames(ast.wrapped);

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast.wrapped);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new InvalidASTException(input.moduleIdentifier, def);
            }
            // In this case the project path doesn't really matter, for calling auxSignatures in addStrategyData...
            final String projectPath =
                Optional.ofNullable(moduleImportService.fileName(input.moduleIdentifier))
                    .orElseGet(input.moduleIdentifier::moduleString);
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                    // Resolving imports here saves us from having to do in the multiple other tasks
                    // that use resolved imports, but it there are often changes to the
                    // strFileGeneratingTasks we may want to pull it into a separate task.
                    imports.addAll(expandImports(context, moduleImportService, def.getSubterm(0),
                        ast.lastModified, messages, input.importResolutionInfo));
                    break;
                case "Signature":
                    for(IStrategoTerm sdecl : def.getSubterm(0)) {
                        switch(TermUtils.toAppl(sdecl).getName()) {
                            case "Constructors":
                                addSigData(input.moduleIdentifier, constrData, externalConstrData,
                                    injections, externalInjections, sdecl.getSubterm(0));
                                break;
                            case "Sorts":
                                addSortData(messages, ast.lastModified, sortData, externalSortData,
                                    sdecl.getSubterm(0));
                                break;
                            default:
                                throw new InvalidASTException(input.moduleIdentifier, sdecl);
                        }
                    }
                    break;
                case "Overlays":
                    addOverlayData(input.moduleIdentifier, overlayUsedConstrs, overlayData,
                        overlayAsts, constrData,
                        def.getSubterm(0));
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    // TODO: test that module is not a source dep from another project
                    final boolean moduleInProject = !input.moduleIdentifier.isLibrary();
                    addStrategyData(context, input.moduleIdentifier, strategyData, internalStrategyData,
                        externalStrategyData, dynamicRuleData, dynamicRules, def.getSubterm(0),
                        projectPath, messages, input.importResolutionInfo, moduleInProject, ast.lastModified);
                    break;
                default:
                    throw new InvalidASTException(input.moduleIdentifier, def);
            }
        }
        if(input.autoImportStd && input.moduleIdentifier.legacyStratego() && !imports
            .contains(BuiltinLibraryIdentifier.StrategoLib)) {
            imports.add(BuiltinLibraryIdentifier.StrategoLib);
        }

        new UsedNamesFront(usedConstructors, usedStrategies, usedAmbiguousStrategies)
            .visit(ast.wrapped);

        return new ModuleData(input.moduleIdentifier, str2LibPackageNames, ast.wrapped, imports,
            sortData, externalSortData, constrData, externalConstrData, injections,
            externalInjections, strategyData, internalStrategyData, externalStrategyData,
            dynamicRuleData, overlayData, overlayAsts, overlayUsedConstrs, usedConstructors,
            usedStrategies, dynamicRules, usedAmbiguousStrategies, messages, ast.lastModified);
    }

    private IStrategoString resourcePathToTerm(ResourcePath resourcePath) {
        final String filename = resourcePathConverter.toString(resourcePath);
        final IStrategoString filenameTerm = tf.makeString(filename);
        filenameTerm.putAttachment(ImploderAttachment.createCompactPositionAttachment("file://" + filename, 0, 0, 0, 0));
        return filenameTerm;
    }

    public static HashSet<IModuleImportService.ModuleIdentifier> expandImports(ExecContext context,
        IModuleImportService moduleImportService, Iterable<IStrategoTerm> imports,
        long lastModified, @Nullable ArrayList<Message> messages,
        ImportResolutionInfo importResolutionInfo)
        throws IOException, ExecException {
        final HashSet<IModuleImportService.ModuleIdentifier> expandedImports = new HashSet<>();
        for(IStrategoTerm anImport : imports) {
            final ImportResolution importResolution =
                moduleImportService.resolveImport(context, anImport, importResolutionInfo);
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

    public static IStrategoList getDefs(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm ast) {
        if(TermUtils.isAppl(ast, "Str2Lib", 3)) {
            final IStrategoList modules = TermUtils.toListAt(ast, 2);
            if(modules.size() == 1) {
                ast = modules.getSubterm(0);
            }
        }
        if(TermUtils.isAppl(ast, "Module", 2)) {
            final IStrategoTerm defs = ast.getSubterm(1);
            if(TermUtils.isList(defs)) {
                return TermUtils.toList(defs);
            }
        } else if(TermUtils.isAppl(ast, "Specification", 1)) {
            final IStrategoTerm defs = ast.getSubterm(0);
            if(TermUtils.isList(defs)) {
                return TermUtils.toList(defs);
            }
        }
        throw new InvalidASTException(moduleIdentifier, ast);
    }

    @Override public String getId() {
        return id;
    }

    protected void addStrategyData(ExecContext context, IModuleImportService.ModuleIdentifier moduleIdentifier,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> strategyData,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> internalStrategyData,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> externalStrategyData,
        LinkedHashMap<StrategySignature, ArrayList<StrategyFrontData>> dynamicRuleData,
        LinkedHashMap<StrategySignature, TreeSet<StrategySignature>> dynamicRules, IStrategoTerm strategyDefs,
        String projectPath, ArrayList<Message> messages, ImportResolutionInfo importResolutionInfo, boolean moduleInProject,
        long lastModified) throws ExecException {
        /*
        def-type-pair: DefHasType(name, t@FunNoArgsType(_, _)) -> ((name, 0, 0), <try(desugar-SType)> t)
        def-type-pair: DefHasType(name, t@FunType(sarg*, _)) -> ((name, <length> sarg*, 0), <try(desugar-SType)> t)
        def-type-pair: DefHasType(name, t@FunTType(sarg*, targ*, _)) ->
          ((name, <length> sarg*, <length> targ*), <try(desugar-SType)> t)
         */
        /*
        m-def-signature =
          try(Desugar)
          ; (  \ SDefT(f, xs, ys, s)      -> (f, <length>xs, <length>ys) \
            <+ \ ExtSDef(f, xs, ys)       -> (f, <length>xs, <length>ys) \
            <+ \ ExtSDefInl(f, xs, ys, s) -> (f, <length>xs, <length>ys) \
            <+ \ RDefT(f, xs, ys, s)      -> (f, <length>xs, <length>ys) \
            <+ \ DefHasType(f, FunTType(xs, ys, _, _)) -> (f, <length>xs, <length>ys) \
            <+ \ AnnoDef(a*, def)         -> <m-def-signature> def\)
         */
        for(IStrategoTerm strategyDef : strategyDefs) {
            StrategyFrontData.Kind kind = Normal;
            HashMap<StrategySignature, ArrayList<StrategyFrontData>> dataMap = strategyData;
            switch(TermUtils.toAppl(strategyDef).getName()) {
                case "DefHasTType":
                case "DefHasType":
                case "DefHasTypeNoArgs": {
                    final @Nullable StrategyType strategyType = StrategyType.fromDefinition(tf, strategyDef);
                    if(strategyType == null) {
                        throw new InvalidASTException(moduleIdentifier, strategyDef);
                    }
                    final StrategySignature strategySignature =
                        strategyType.withName(TermUtils.toStringAt(strategyDef, 0));
                    Relation.getOrInitialize(strategyData, strategySignature, ArrayList::new)
                        .add(new StrategyFrontData(strategySignature, strategyType, TypeDefinition));
                    break;
                }
                case "ExtTypedDef":
                case "ExtTypedDefInl": {
                    final @Nullable StrategyType strategyType = StrategyType.fromDefinition(tf, strategyDef);
                    if(strategyType == null) {
                        throw new InvalidASTException(moduleIdentifier, strategyDef);
                    }
                    final StrategySignature strategySignature =
                        strategyType.withName(TermUtils.toStringAt(strategyDef, 0));
                    Relation.getOrInitialize(externalStrategyData, strategySignature, ArrayList::new)
                        .add(new StrategyFrontData(strategySignature, strategyType, TypeDefinition));
                    Relation.getOrInitialize(externalStrategyData, strategySignature, ArrayList::new)
                        .add(new StrategyFrontData(strategySignature, strategyType, External));
                    break;
                }
                case "ExtSDef":
                case "ExtSDefInl":
                    kind = External;
                    dataMap = externalStrategyData;
                    // fallthrough
                default:
                    if(TermUtils.isAppl(strategyDef, "AnnoDef", 2)) {
                        for(IStrategoTerm anno : strategyDef.getSubterm(0)) {
                            if(TermUtils.isAppl(anno, "Internal", 0)) {
                                kind = Internal;
                                dataMap = internalStrategyData;
                            } else if(TermUtils.isAppl(anno, "Extend", 0)) {
                                kind = Extend;
                            } else if(TermUtils.isAppl(anno, "Override", 0)) {
                                kind = StrategyFrontData.Kind.Override;
                            }
                        }
                        strategyDef = strategyDef.getSubterm(1);
                    }
                    final @Nullable StrategySignature strategySignature =
                        StrategySignature.fromDefinition(strategyDef);
                    if(moduleInProject && importResolutionInfo.resolveExternals != null && kind == External) {
                        if(!moduleImportService.externalStrategyExists(context, strategySignature, importResolutionInfo)) {
                            messages.add(ExternalStrategySourceNotFound.followOrigin(TermUtils.toString(strategySignature.getSubterm(0)), lastModified));
                        }
                    }
                    if(strategySignature == null) {
                        throw new InvalidASTException(moduleIdentifier, strategyDef);
                    }
                    Relation.getOrInitialize(dataMap, strategySignature, ArrayList::new).add(
                        new StrategyFrontData(strategySignature, strategySignature.standardType(tf),
                            kind));
                    break;
            }

            // collect-om(dyn-rule-sig)
            final TreeSet<StrategySignature> auxSignatures = new TreeSet<>();
            auxRuleSigs(strategyDef, auxSignatures, projectPath);
            for(StrategySignature dynRuleSig : CollectDynRuleSigs.collect(strategyDef)) {
                final TreeSet<StrategySignature> strategySignatures =
                    new TreeSet<>(dynRuleSig.dynamicRuleSignatures(tf).keySet());
                for(Iterator<StrategySignature> iterator = auxSignatures.iterator(); iterator.hasNext(); ) {
                    StrategySignature auxSignature = iterator.next();
                    if(auxSignature.name.startsWith("aux-" + dynRuleSig.name)) {
                        strategySignatures.add(auxSignature);
                        iterator.remove();
                    }
                }
                for(StrategySignature signature : strategySignatures) {
                    Relation.getOrInitialize(dynamicRuleData, signature, ArrayList::new).add(
                        new StrategyFrontData(signature, signature.standardType(tf),
                            DynRuleGenerated));
                }
                Relation.getOrInitialize(dynamicRules, dynRuleSig, TreeSet::new).addAll(strategySignatures);
            }
        }
    }

    private void auxRuleSigs(IStrategoTerm strategyDef, Set<StrategySignature> dynRuleSigs,
        String projectPath) throws ExecException {
        final IStrategoTerm result = strategoLanguage.auxSignatures(strategyDef, projectPath);
        for(IStrategoTerm sig : TermUtils.toList(result)) {
            assert TermUtils.isTuple(sig, 3);
            dynRuleSigs.add(
                new StrategySignature(TermUtils.toStringAt(sig, 0), TermUtils.toJavaIntAt(sig, 1),
                    TermUtils.toJavaIntAt(sig, 2)));
        }
    }

    protected void addOverlayData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        HashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData,
        HashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts, HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData,
        IStrategoTerm overlays) {
        for(IStrategoTerm overlay : overlays) {
            final int arity;
            if(!TermUtils.isStringAt(overlay, 0)) {
                throw new InvalidASTException(moduleIdentifier, overlay);
            }
            final String name = TermUtils.toJavaStringAt(overlay, 0);
            switch(TermUtils.toAppl(overlay).getName()) {
                case "OverlayNoArgs": {
                    if(overlay.getSubtermCount() != 2) {
                        throw new InvalidASTException(moduleIdentifier, overlay);
                    }
                    arity = 0;
                    addOverlayUsedConstrs(overlayUsedConstrs, overlayAsts, overlay, arity, name);
                    break;
                }
                case "Overlay": {
                    if(overlay.getSubtermCount() != 3 || !TermUtils.isListAt(overlay, 1)) {
                        throw new InvalidASTException(moduleIdentifier, overlay);
                    }
                    arity = TermUtils.toListAt(overlay, 1).size();
                    addOverlayUsedConstrs(overlayUsedConstrs, overlayAsts, overlay, arity, name);
                    break;
                }
                case "OverlayDeclNoArgs": {
                    if(overlay.getSubtermCount() != 2) {
                        throw new InvalidASTException(moduleIdentifier, overlay);
                    }
                    arity = 0;
                    addOverlayDeclData(moduleIdentifier, overlayData, constrData, overlay, arity, name);
                    break;
                }
                case "OverlayDecl": {
                    if(overlay.getSubtermCount() != 3 || !TermUtils.isListAt(overlay, 1)) {
                        throw new InvalidASTException(moduleIdentifier, overlay);
                    }
                    arity = TermUtils.toListAt(overlay, 1).size();
                    addOverlayDeclData(moduleIdentifier, overlayData, constrData, overlay, arity, name);
                    break;
                }
                default:
                    throw new InvalidASTException(moduleIdentifier, overlay);
            }
        }
    }

    private void addOverlayDeclData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> overlayData,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData, IStrategoTerm overlay, final int arity,
        final String name) {
        final @Nullable ConstructorType type = ConstructorType.fromOverlayDecl(tf, overlay);
        if(type == null) {
            throw new InvalidASTException(moduleIdentifier, overlay);
        }
        final ConstructorSignature signature = new ConstructorSignature(name, arity);
        final ConstructorData data =
            new ConstructorData(signature, type, true);
        Relation.getOrInitialize(constrData, signature, ArrayList::new).add(data);
        Relation.getOrInitialize(overlayData, signature, ArrayList::new).add(data);
    }

    private void addOverlayUsedConstrs(HashMap<ConstructorSignature, LinkedHashSet<ConstructorSignature>> overlayUsedConstrs,
        HashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayAsts,
        IStrategoTerm overlay, final int arity, final String name) {
        final LinkedHashSet<ConstructorSignature> usedConstructors = new LinkedHashSet<>();
        new UsedConstrs(usedConstructors).visit(overlay);
        final ConstructorSignature signature = new ConstructorSignature(name, arity);
        Relation.getOrInitialize(overlayUsedConstrs, signature, LinkedHashSet::new).addAll(usedConstructors);
        Relation.getOrInitialize(overlayAsts, signature, ArrayList::new).add(overlay);
    }

    private void addSortData(ArrayList<Message> messages, long lastModified,
        LinkedHashSet<SortSignature> sortData, LinkedHashSet<SortSignature> externalSortData,
        IStrategoTerm sorts) {
        if(!TermUtils.isList(sorts)) {
            messages.add(new InvalidASTMessage(sorts, lastModified, "a list"));
            return;
        }
        for(IStrategoTerm sortDef : sorts) {
            final @Nullable SortSignature sortSig = SortSignature.fromTerm(sortDef);
            final LinkedHashSet<SortSignature> dataSet;
            final @Nullable Boolean external = SortSignature.isExternal(sortDef);
            if(sortSig == null || external == null) {
                messages
                    .add(new InvalidASTMessage(sortDef, lastModified, "a valid sort definition"));
                continue;
            }
            if(external) {
                dataSet = externalSortData;
            } else {
                dataSet = sortData;
            }
            dataSet.add(sortSig);
        }
    }

    protected void addSigData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections,
        IStrategoTerm constrs) {
        if(!TermUtils.isList(constrs)) {
            throw new InvalidASTException(moduleIdentifier, constrs);
        }
        for(IStrategoTerm constrDef : constrs) {
            final @Nullable ConstructorSignature constrSig =
                ConstructorSignature.fromTerm(constrDef);
            if(constrSig == null) {
                addInjectionData(moduleIdentifier, constrDef, injections, externalInjections,
                    constrData);
                continue;
            }
            final ConstructorType constrType = constrType(moduleIdentifier, constrDef);
            final HashMap<ConstructorSignature, ArrayList<ConstructorData>> dataMap;
            final @Nullable Boolean external = ConstructorSignature.isExternal(constrDef);
            if(external == null) {
                throw new InvalidASTException(moduleIdentifier, constrDef);
            }
            if(external) {
                dataMap = externalConstrData;
            } else {
                dataMap = constrData;
            }
            Relation.getOrInitialize(dataMap, constrSig, ArrayList::new)
                .add(new ConstructorData(constrSig, constrType));
        }
    }

    private ConstructorType constrType(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm constrDef) {
        /*
        extract-constr =
          (  ?OpDecl(c, ConstType(t1))
          <+ OpDeclQ(c, ConstType(t1))
          <+ ExtOpDecl(c, ConstType(t1))
          <+ ExtOpDeclQ(c, ConstType(t1)) )
        ; with(t2 := <desugar-Type> t1)
        ; !((c,0), ConstrType([], t2))

        extract-constr:
          (  ?OpDecl(c, FunType(t1*, ConstType(t1)))
          <+ OpDeclQ(c, FunType(t1*, ConstType(t1)))
          <+ ExtOpDecl(c, FunType(t1*, ConstType(t1)))
          <+ ExtOpDeclQ(c, FunType(t1*, ConstType(t1))) )
        ; with(
            t2 := <desugar-Type> t1
          ; t2* := <map(?ConstType(<desugar-Type>) <+ ?DynT())> t1*)
        ; !((c, <length> t1*), ConstrType(t2*, t2))
         */
        if(!TermUtils.isAppl(constrDef)) {
            throw new InvalidASTException(moduleIdentifier, constrDef);
        }
        switch(TermUtils.toAppl(constrDef).getName()) {
            case "OpDecl":
                // fall-through
            case "OpDeclQ":
                // fall-through
            case "ExtOpDecl":
                // fall-through
            case "ExtOpDeclQ":
                break;
            default:
                throw new InvalidASTException(moduleIdentifier, constrDef);
        }

        final IStrategoTerm opType = constrDef.getSubterm(1);
        if(!TermUtils.isAppl(opType)) {
            throw new InvalidASTException(moduleIdentifier, opType);
        }

        final @Nullable ConstructorType type = ConstructorType.fromOpType(tf, opType);

        if(type == null) {
            throw new InvalidASTException(moduleIdentifier, opType);
        }

        return type;
    }

    private void addInjectionData(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm constrDef, HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections,
        HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData) {
        /*
        extract-inj:
          OpDeclInj(FunType([ConstType(from)], ConstType(to))) ->
            (<desugar-Type> from, <desugar-Type> to)

        extract-inj:
          ExtOpDeclInj(FunType([ConstType(from)], ConstType(to))) ->
            (<desugar-Type> from, <desugar-Type> to)

        extract-inj:
          OpDeclInj(FunType(t1*@[_, _ | _], ConstType(t1))) ->
            (Sort("Tuple", t2*), t2)
          with
            t2 := <desugar-Type> t1
          ; t2* := <map(?ConstType(<desugar-Type>) <+ ?DynT())> t1*

        extract-inj:
          OpDeclInj(ConstType(t1)) -> (Sort("Tuple", []), t2)
          with
            t2 := <desugar-Type> t1

        extract-constr:
          ExtOpDeclInj(FunType(t1*@[_, _ | _], ConstType(t1))) ->
            (("", <length> t1*), ConstrType(t2*, t2))
          with
            t2 := <desugar-Type> t1
          ; t2* := <map(?ConstType(<desugar-Type>) <+ ?DynT())> t1*

        extract-constr:
          ExtOpDeclInj(ConstType(t1)) -> (("", 0), ConstrType([], t2))
          with
            t2 := <desugar-Type> t1
         */
        if(!TermUtils.isAppl(constrDef)) {
            throw new InvalidASTException(moduleIdentifier, constrDef);
        }
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> dataMap = externalInjections;
        switch(TermUtils.toAppl(constrDef).getName()) {
            case "OpDeclInj":
                dataMap = injections;
                // fall-through
            case "ExtOpDeclInj":
                if(constrDef.getSubtermCount() != 1) {
                    throw new InvalidASTException(moduleIdentifier, constrDef);
                }
                final IStrategoTerm opType = constrDef.getSubterm(0);
                final @Nullable ConstructorType constrType = ConstructorType.fromOpType(tf, opType);
                if(constrType == null) {
                    throw new InvalidASTException(moduleIdentifier, opType);
                }

                final IStrategoTerm from;
                final List<IStrategoTerm> froms = constrType.getFrom();
                if(froms.size() == 1) {
                    from = froms.get(0);
                } else {
                    final IStrategoList tupleTypes = tf.makeList(froms);
                    from = tf.makeAppl("Sort", tf.makeString("Tuple"), tupleTypes);

                    final ConstructorSignature constrSig =
                        new ConstructorSignature("", froms.size());
                    Relation.getOrInitialize(constrData, constrSig, ArrayList::new).add(
                        new ConstructorData(constrSig, constrType));
                }
                Relation.getOrInitialize(dataMap, from, ArrayList::new).add(constrType.to);
        }
    }

    protected LastModified<IStrategoTerm> getModuleAst(ExecContext context, FrontInput input)
        throws Exception {
        if(input instanceof FrontInput.Normal) {
            return getModuleAst(context, (FrontInput.Normal) input);
        } else if(input instanceof FrontInput.FileOpenInEditor) {
            final LastModified<IStrategoTerm> editorAst = ((FrontInput.FileOpenInEditor) input).ast;
            return LastModified.fromParent(strategoLanguage.metaExplode(editorAst.wrapped), editorAst);
        } else {
            throw new RuntimeException("Unknown subclass of FrontInput: " + input.getClass());
        }
    }

    private LastModified<IStrategoTerm> getModuleAst(ExecContext context, FrontInput.Normal input)
        throws Exception {
        return moduleImportService.getModuleAst(context, input.moduleIdentifier,
            input.importResolutionInfo);
    }
}
