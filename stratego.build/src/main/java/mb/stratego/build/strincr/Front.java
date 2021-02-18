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
import mb.stratego.build.termvisitors.UsedNamesFront;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.StrIncrContext;

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

        @Override public String toString() {
            return "Front.Input(" + moduleIdentifier + ")";
        }
    }

    @Override public ModuleData exec(ExecContext context, Input input) throws Exception {
        final LastModified<IStrategoTerm> ast =
            input.moduleImportService.getModuleAst(context, input.moduleIdentifier);
        boolean stdLibImport = false;
        final List<IStrategoTerm> imports = new ArrayList<>();
        final Map<ConstructorSignature, List<ConstructorData>> constrData = new HashMap<>();
        final Map<ConstructorSignature, List<OverlayData>> overlayData = new HashMap<>();
        final Map<StrategySignature, Set<StrategyFrontData>> strategyData = new HashMap<>();
        final Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData = new HashMap<>();
        final Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData = new HashMap<>();
        final Map<StrategySignature, Set<StrategyFrontData>> dynamicRuleData = new HashMap<>();
        final Set<StrategySignature> dynamicRules = new HashSet<>();
        final Map<IStrategoTerm, List<IStrategoTerm>> injections = new HashMap<>();

        final IStrategoList defs = getDefs(input.moduleIdentifier, ast.wrapped);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new WrongASTException(input.moduleIdentifier, def);
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
                    addSigData(input.moduleIdentifier, constrData, injections, def.getSubterm(0),
                        ast.lastModified);
                    break;
                case "Overlays":
                    addOverlayData(input.moduleIdentifier, overlayData, constrData, def.getSubterm(0),
                        ast.lastModified);
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    addStrategyData(input.moduleIdentifier, strategyData, internalStrategyData,
                        externalStrategyData, dynamicRuleData, dynamicRules, def.getSubterm(0));
                    break;
                default:
                    throw new WrongASTException(input.moduleIdentifier, def);
            }
        }
        if(!stdLibImport) {
            imports.add(tf.makeAppl("Import", tf.makeString("libstratego-lib")));
        }

        final Set<ConstructorSignature> usedConstructors = new HashSet<>();
        final Set<StrategySignature> usedStrategies = new HashSet<>();
        final Set<String> usedAmbiguousStrategies = new HashSet<>();
        new UsedNamesFront(usedConstructors, usedStrategies, usedAmbiguousStrategies, ast.lastModified)
            .visit(ast.wrapped);

        return new ModuleData(input.moduleIdentifier, ast.wrapped, imports, constrData, injections,
            strategyData, internalStrategyData, externalStrategyData, dynamicRuleData, overlayData, usedConstructors,
            usedStrategies, dynamicRules, usedAmbiguousStrategies, ast.lastModified);
    }

    public static IStrategoList getDefs(ModuleIdentifier moduleIdentifier, IStrategoTerm ast)
        throws WrongASTException {
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
