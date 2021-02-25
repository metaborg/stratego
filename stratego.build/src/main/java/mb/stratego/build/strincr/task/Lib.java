package mb.stratego.build.strincr.task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

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
        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> constrData =
            new LinkedHashMap<>(0);
        final LinkedHashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData =
            new LinkedHashMap<>(0);
        final LinkedHashSet<ConstructorSignature> usedConstructors = new LinkedHashSet<>(0);
        final LinkedHashSet<StrategySignature> usedStrategies = new LinkedHashSet<>(0);
        final LinkedHashSet<String> usedAmbiguousStrategies = new LinkedHashSet<>(0);
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> strategyData =
            new LinkedHashMap<>(0);
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>>
            internalStrategyData = new LinkedHashMap<>(0);
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>> dynamicRuleData =
            new LinkedHashMap<>(0);
        final LinkedHashSet<StrategySignature> dynamicRules = new LinkedHashSet<>(0);
        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections =
            new LinkedHashMap<>(0);

        final LinkedHashMap<ConstructorSignature, ArrayList<ConstructorData>> externalConstrData =
            new LinkedHashMap<>();
        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> externalInjections =
            new LinkedHashMap<>();
        final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyFrontData>>
            externalStrategyData = new LinkedHashMap<>();

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
