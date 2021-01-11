package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.termvisitors.UsedNamesFront;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.TermWithLastModified;

/**
 * Task that takes a {@link ModuleIdentifier} and processes the corresponding AST. The AST is split
 * into {@link ModuleData}, which contains the original AST along with several lists of
 * information required in other tasks.
 */
public class Front extends SplitShared implements TaskDef<Front.Input, ModuleData> {
    public static final String id = Front.class.getCanonicalName();

    @Inject public Front(StrIncrContext strContext) {
        super(strContext);
    }

    public static class Input implements Serializable {
        public final ModuleIdentifier moduleIdentifier;
        public final IModuleImportService moduleImportService;

        public Input(ModuleIdentifier moduleIdentifier, IModuleImportService moduleImportService) {
            this.moduleIdentifier = moduleIdentifier;
            this.moduleImportService = moduleImportService;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!moduleIdentifier.equals(input.moduleIdentifier))
                return false;
            return moduleImportService.equals(input.moduleImportService);
        }

        @Override public int hashCode() {
            int result = moduleIdentifier.hashCode();
            result = 31 * result + moduleImportService.hashCode();
            return result;
        }
    }

    @Override public ModuleData exec(ExecContext context, Input input) throws Exception {
        final TermWithLastModified ast =
            input.moduleImportService.getModuleAst(context, input.moduleIdentifier);
        final List<TermWithLastModified> imports = new ArrayList<>();
        final Map<ConstructorSignature, List<ConstructorData>> constrData = new HashMap<>();
        final Map<ConstructorSignature, List<ConstructorData>> overlayData = new HashMap<>();
        final Map<StrategySignature, Set<StrategyFrontData>> strategyData = new HashMap<>();
        final Map<TermWithLastModified, List<TermWithLastModified>> injections = new HashMap<>();
        final List<Message<?>> messages = new ArrayList<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new WrongASTException(input.moduleIdentifier, def);
            }
            final TermWithLastModified defWLM =
                TermWithLastModified.fromParent(def.getSubterm(0), ast);
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                    for(IStrategoTerm importTerm : def.getSubterm(0)) {
                        imports.add(TermWithLastModified.fromParent(importTerm, ast));
                    }
                    break;
                case "Signature":
                    addSigData(input.moduleIdentifier, constrData, injections, defWLM);
                    break;
                case "Overlays":
                    addOverlayData(input.moduleIdentifier, overlayData, constrData, defWLM);
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    addStrategyData(input.moduleIdentifier, strategyData, defWLM, messages);
                    break;
                default:
                    throw new WrongASTException(input.moduleIdentifier, def);
            }
        }

        final Set<ConstructorSignature> usedConstructors = new HashSet<>();
        final Set<StrategySignature> usedStrategies = new HashSet<>();
        final Set<String> usedAmbiguousStrategies = new HashSet<>();
        new UsedNamesFront(usedConstructors, usedStrategies, usedAmbiguousStrategies).visit(ast.term);

        return new ModuleData(input.moduleIdentifier, ast, imports, constrData, injections,
            strategyData, overlayData, usedConstructors, usedStrategies, usedAmbiguousStrategies,
            messages);
    }

    private static IStrategoList getDefs(ModuleIdentifier moduleIdentifier,
        TermWithLastModified timestampedAst) throws WrongASTException {
        final IStrategoTerm ast = timestampedAst.term;
        if(TermUtils.isAppl(ast, "Module", 2)) {
            final IStrategoTerm defs = ast.getSubterm(1);
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
