package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
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
        boolean stdLibImport = false;
        final ArrayList<IStrategoTerm> imports = new ArrayList<>();
        final HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData =
            new HashMap<>();
        final HashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData =
            new HashMap<>();
        final HashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData = new HashMap<>();
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> strategyData = new HashMap<>();
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> internalStrategyData =
            new HashMap<>();
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> externalStrategyData =
            new HashMap<>();
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> dynamicRuleData =
            new HashMap<>();
        final HashSet<StrategySignature> dynamicRules = new HashSet<>();
        final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections = new HashMap<>();
        final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections = new HashMap<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast.wrapped);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new InvalidASTException(input.moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                    for(IStrategoTerm importTerm : def.getSubterm(0)) {
                        imports.add(importTerm);
                        if(TermUtils.isStringAt(importTerm, 0)) {
                            switch(TermUtils.toJavaStringAt(importTerm, 0)) {
                                case "stratego-lib":
                                case "libstrategolib":
                                case "libstratego-lib":
                                    stdLibImport = true;
                            }
                        }
                    }
                    break;
                case "Signature":
                    addSigData(input.moduleIdentifier, constrData, externalConstrData, injections, externalInjections,
                        def.getSubterm(0), ast.lastModified);
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
        if(!stdLibImport) {
            imports.add(tf.makeAppl("Import", tf.makeString("libstratego-lib")));
        }

        final HashSet<ConstructorSignature> usedConstructors = new HashSet<>();
        final HashSet<StrategySignature> usedStrategies = new HashSet<>();
        final HashSet<String> usedAmbiguousStrategies = new HashSet<>();
        new UsedNamesFront(usedConstructors, usedStrategies, usedAmbiguousStrategies,
            ast.lastModified).visit(ast.wrapped);

        return new ModuleData(input.moduleIdentifier, ast.wrapped, imports, constrData,
            externalConstrData, injections, externalInjections, strategyData, internalStrategyData,
            externalStrategyData, dynamicRuleData, overlayData, usedConstructors, usedStrategies,
            dynamicRules, usedAmbiguousStrategies, ast.lastModified);
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
