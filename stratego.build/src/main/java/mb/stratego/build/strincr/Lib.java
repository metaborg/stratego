package mb.stratego.build.strincr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.StrIncrContext;

public class Lib extends SplitShared implements TaskDef<Front.Input, ModuleData> {
    public static final String id = Lib.class.getCanonicalName();

    @Inject public Lib(StrIncrContext strContext) {
        super(strContext);
    }

    @Override public ModuleData exec(ExecContext context, Front.Input input) throws Exception {
        final List<IStrategoTerm> imports = Collections.emptyList();
        final Map<ConstructorSignature, List<OverlayData>> overlayData = Collections.emptyMap();
        final Set<ConstructorSignature> usedConstructors = Collections.emptySet();
        final Set<StrategySignature> usedStrategies = Collections.emptySet();
        final Set<String> usedAmbiguousStrategies = Collections.emptySet();
        final Map<StrategySignature, Set<StrategyFrontData>> strategyData = Collections.emptyMap();
        final Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData =
            Collections.emptyMap();

        final LastModified<IStrategoTerm> ast =
            input.moduleImportService.getModuleAst(context, input.moduleIdentifier);
        final Map<ConstructorSignature, List<ConstructorData>> constrData = new HashMap<>();
        final Map<IStrategoTerm, List<IStrategoTerm>> injections = new HashMap<>();
        final Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData = new HashMap<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new WrongASTException(input.moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Signature":
                    addSigData(input.moduleIdentifier, constrData, injections, def,
                        ast.lastModified);
                    break;
                case "Strategies":
                    addStrategyData(input.moduleIdentifier, strategyData, internalStrategyData,
                        strategyData, def);
                    break;
                default:
                    throw new WrongASTException(input.moduleIdentifier, def);
            }
        }
        ;
        return new ModuleData(input.moduleIdentifier, ast.wrapped, imports, constrData, injections,
            strategyData, internalStrategyData, externalStrategyData, overlayData, usedConstructors,
            usedStrategies, usedAmbiguousStrategies, ast.lastModified);
    }

    private IStrategoList getDefs(ModuleIdentifier moduleIdentifier,
        LastModified<IStrategoTerm> timestampedAst) throws WrongASTException {
        final IStrategoTerm ast = timestampedAst.wrapped;
        if(TermUtils.isAppl(ast, "Specification", 1)) {
            final IStrategoTerm defs = ast.getSubterm(0);
            if(TermUtils.isList(defs)) {
                return TermUtils.toList(defs);
            }
        }
        throw new WrongASTException(moduleIdentifier, ast);
    }

    @Override public String getId() {
        return id;
    }
}
