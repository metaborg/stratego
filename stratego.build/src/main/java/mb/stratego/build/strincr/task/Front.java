package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportService.ImportResolution;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.UnresolvedImport;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.termvisitors.UsedNamesFront;
import mb.stratego.build.util.InvalidASTException;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.StrIncrContext;

/**
 * Task that takes a {@link IModuleImportService.ModuleIdentifier} and processes the corresponding AST. The AST is split
 * into {@link ModuleData}, which contains the original AST along with several lists of
 * information required in other task.
 */
public class Front extends SplitShared implements TaskDef<FrontInput, ModuleData> {
    public static final String id = "stratego." + Front.class.getSimpleName();

    @Inject public Front(StrIncrContext strContext, IModuleImportService moduleImportService) {
        super(strContext, moduleImportService);
    }

    @Override public ModuleData exec(ExecContext context, FrontInput input) throws Exception {
        final LastModified<IStrategoTerm> ast = getModuleAst(context, input);
        final ArrayList<IModuleImportService.ModuleIdentifier> imports = new ArrayList<>();
        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData =
            new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData =
            new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> strategyData =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>>
            internalStrategyData = new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>>
            externalStrategyData = new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> dynamicRuleData =
            new LinkedHashMap<>();
        final LinkedHashSet<StrategySignature> dynamicRules = new LinkedHashSet<>();
        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections =
            new LinkedHashMap<>();
        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections =
            new LinkedHashMap<>();
        final ArrayList<Message> messages = new ArrayList<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast.wrapped);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new InvalidASTException(input.moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                    // Resolving imports here saves us from having to do in the multiple other tasks
                    // that use resolved imports, but it there are often changes to the
                    // strFileGeneratingTasks we may want to pull it into a separate task.
                    imports.addAll(expandImports(context, moduleImportService, def.getSubterm(0),
                        ast.lastModified, messages, input.strFileGeneratingTasks, input.includeDirs,
                        input.linkedLibraries));
                    break;
                case "Signature":
                    addSigData(input.moduleIdentifier, constrData, externalConstrData, injections,
                        externalInjections, def.getSubterm(0), ast.lastModified);
                    break;
                case "Overlays":
                    addOverlayData(input.moduleIdentifier, overlayData, constrData,
                        def.getSubterm(0), ast.lastModified);
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    addStrategyData(input.moduleIdentifier, strategyData, internalStrategyData,
                        externalStrategyData, dynamicRuleData, dynamicRules, def.getSubterm(0));
                    break;
                default:
                    throw new InvalidASTException(input.moduleIdentifier, def);
            }
        }
        if(!imports.contains(BuiltinLibraryIdentifier.StrategoLib)) {
            imports.add(BuiltinLibraryIdentifier.StrategoLib);
        }

        final LinkedHashSet<ConstructorSignature> usedConstructors = new LinkedHashSet<>();
        final LinkedHashSet<StrategySignature> usedStrategies = new LinkedHashSet<>();
        final LinkedHashSet<String> usedAmbiguousStrategies = new LinkedHashSet<>();
        new UsedNamesFront(usedConstructors, usedStrategies, usedAmbiguousStrategies,
            ast.lastModified).visit(ast.wrapped);

        return new ModuleData(input.moduleIdentifier, ast.wrapped, imports, constrData,
            externalConstrData, injections, externalInjections, strategyData, internalStrategyData,
            externalStrategyData, dynamicRuleData, overlayData, usedConstructors, usedStrategies,
            dynamicRules, usedAmbiguousStrategies, messages, ast.lastModified);
    }

    public static HashSet<IModuleImportService.ModuleIdentifier> expandImports(ExecContext context,
        IModuleImportService moduleImportService, Iterable<IStrategoTerm> imports,
        long lastModified, @Nullable ArrayList<Message> messages,
        Collection<STask<?>> strFileGeneratingTasks, Collection<? extends ResourcePath> includeDirs,
        Collection<? extends IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws IOException, ExecException {
        final HashSet<IModuleImportService.ModuleIdentifier> expandedImports = new HashSet<>();
        for(IStrategoTerm anImport : imports) {
            final ImportResolution importResolution = moduleImportService
                .resolveImport(context, anImport, strFileGeneratingTasks, includeDirs,
                    linkedLibraries);
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
}
