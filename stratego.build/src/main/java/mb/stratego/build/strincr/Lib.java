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
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.TermWithLastModified;

public class Lib extends SplitShared implements TaskDef<Front.Input, ModuleData> {
    public static final String id = Lib.class.getCanonicalName();

    @Inject public Lib(StrIncrContext strContext) {
        super(strContext);
    }

    @Override public ModuleData exec(ExecContext context, Front.Input input) throws Exception {
        final List<TermWithLastModified> imports = Collections.emptyList();
        final Map<ConstructorSignature, List<ConstructorData>> overlayData = Collections.emptyMap();
        final List<Message<?>> messages = Collections.emptyList();
        final Set<ConstructorSignature> usedConstructors = Collections.emptySet();
        final Set<StrategySignature> usedStrategies = Collections.emptySet();
        final Set<String> usedAmbiguousStrategies = Collections.emptySet();

        final TermWithLastModified ast =
            input.moduleImportService.getModuleAst(context, input.moduleIdentifier);
        final Map<ConstructorSignature, List<ConstructorData>> constrData = new HashMap<>();
        final Map<TermWithLastModified, List<TermWithLastModified>> injections = new HashMap<>();
        final Map<StrategySignature, Set<StrategyFrontData>> strategyData = new HashMap<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new WrongASTException(input.moduleIdentifier, def);
            }
            final TermWithLastModified defWLM = TermWithLastModified.fromParent(def, ast);
            switch(TermUtils.toAppl(def).getName()) {
                case "Signature":
                    addSigData(input.moduleIdentifier, constrData, injections,
                        defWLM);
                    break;
                case "Strategies":
                    addStrategyData(input.moduleIdentifier, strategyData, defWLM, messages);
                    break;
                default:
                    throw new WrongASTException(input.moduleIdentifier, def);
            }
        };
        return new ModuleData(input.moduleIdentifier, ast, imports, constrData, injections,
            strategyData, overlayData, usedConstructors, usedStrategies, usedAmbiguousStrategies,
            messages);
    }

    private IStrategoList getDefs(IModuleImportService.ModuleIdentifier moduleIdentifier,
        TermWithLastModified timestampedAst) throws WrongASTException {
        final IStrategoTerm ast = timestampedAst.term;
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
