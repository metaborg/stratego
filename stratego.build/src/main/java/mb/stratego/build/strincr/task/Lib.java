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
import mb.stratego.build.util.InvalidASTException;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.StrIncrContext;

/**
 * Task that takes a {@link IModuleImportService.ModuleIdentifier} and processes the corresponding AST. The AST is split
 * into {@link ModuleData}, which contains the original AST along with several lists of
 * information required in other task.
 * This is a specialisation of {@link Front}.
 */
public class Lib extends SplitShared implements TaskDef<FrontInput, ModuleData> {
    public static final String id = "stratego." + Lib.class.getSimpleName();

    @Inject public Lib(StrIncrContext strContext, IModuleImportService moduleImportService) {
        super(strContext, moduleImportService);
    }

    @Override public ModuleData exec(ExecContext context, FrontInput input) throws Exception {
        final LastModified<IStrategoTerm> ast = getModuleAst(context, input);

        final ArrayList<IStrategoTerm> imports = new ArrayList<>(0);
        final HashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData =
            new HashMap<>(0);
        final HashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData = new HashMap<>(0);
        final HashSet<ConstructorSignature> usedConstructors = new HashSet<>(0);
        final HashSet<StrategySignature> usedStrategies = new HashSet<>(0);
        final HashSet<String> usedAmbiguousStrategies = new HashSet<>(0);
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> strategyData =
            new HashMap<>(0);
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> internalStrategyData =
            new HashMap<>(0);
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> dynamicRuleData =
            new HashMap<>(0);
        final HashSet<StrategySignature> dynamicRules = new HashSet<>(0);
        final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections = new HashMap<>(0);

        final HashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData =
            new HashMap<>();
        final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections = new HashMap<>();
        final HashMap<StrategySignature, HashSet<StrategyFrontData>> externalStrategyData =
            new HashMap<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new InvalidASTException(input.moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Signature":
                    addSigData(input.moduleIdentifier, constrData, externalConstrData, injections,
                        externalInjections, def.getSubterm(0), ast.lastModified);
                    break;
                case "Strategies":
                    addStrategyData(input.moduleIdentifier, strategyData, internalStrategyData,
                        externalStrategyData, dynamicRuleData, dynamicRules, def.getSubterm(0));
                    break;
                default:
                    throw new InvalidASTException(input.moduleIdentifier, def);
            }
        }

        return new ModuleData(input.moduleIdentifier, ast.wrapped, imports, constrData,
            externalConstrData, injections, externalInjections, strategyData, internalStrategyData,
            externalStrategyData, dynamicRuleData, overlayData, usedConstructors, usedStrategies,
            dynamicRules, usedAmbiguousStrategies, ast.lastModified);
    }

    private IStrategoList getDefs(IModuleImportService.ModuleIdentifier moduleIdentifier,
        LastModified<IStrategoTerm> timestampedAst) {
        final IStrategoTerm ast = timestampedAst.wrapped;
        if(TermUtils.isAppl(ast, "Specification", 1)) {
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
