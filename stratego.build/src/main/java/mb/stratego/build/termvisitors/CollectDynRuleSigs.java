package mb.stratego.build.termvisitors;

import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.data.StrategySignature;

public class CollectDynRuleSigs {
    private final TreeSet<StrategySignature> signatures = new TreeSet<>();

    public static TreeSet<StrategySignature> collect(IStrategoTerm ast) {
        CollectDynRuleSigs instance = new CollectDynRuleSigs();
        instance.visit(ast);
        return instance.signatures;
    }

    private void visit(IStrategoTerm term) {
        /*
        dyn-rule-sig: RDecNoArgs(name) -> (name, 0, 0)
        dyn-rule-sig: RDec(name, sarg*) -> (name, <length> sarg*, 0)
        dyn-rule-sig: RDecT(name, sarg*, targ*) -> (name, <length> sarg*, <length> targ*)
         */
        if(TermUtils.isAppl(term)) {
            switch(TermUtils.toAppl(term).getName()) {
                case "RDecNoArgs":
                    if(term.getSubtermCount() == 1 && TermUtils.isStringAt(term, 0)) {
                        signatures.add(new StrategySignature(TermUtils.toStringAt(term, 0), 0, 0));
                        return;
                    }
                    break;
                case "RDec":
                    if(term.getSubtermCount() == 2 && TermUtils.isStringAt(term, 0) && TermUtils
                        .isListAt(term, 1)) {
                        signatures.add(new StrategySignature(TermUtils.toStringAt(term, 0),
                            TermUtils.toListAt(term, 1).size(), 0));
                        return;
                    }
                    break;
                case "RDecT":
                    if(term.getSubtermCount() == 3 && TermUtils.isStringAt(term, 0) && TermUtils
                        .isListAt(term, 1) && TermUtils.isListAt(term, 2)) {
                        signatures.add(new StrategySignature(TermUtils.toStringAt(term, 0),
                            TermUtils.toListAt(term, 1).size(),
                            TermUtils.toListAt(term, 2).size()));
                        return;
                    }
                    break;
            }
        }
        for(IStrategoTerm child : term) {
            visit(child);
        }
    }
}
