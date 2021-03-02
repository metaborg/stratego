package mb.stratego.build.termvisitors;

import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.data.StrategySignature;

public class CollectDynRuleSigs {
    private final LinkedHashSet<StrategySignature> signatures = new LinkedHashSet<>();

    public static LinkedHashSet<StrategySignature> collect(IStrategoTerm ast) {
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
        if(TermUtils.isAppl(term, "RDecNoArgs", 1) && TermUtils.isStringAt(term, 0)) {
            signatures.add(new StrategySignature(TermUtils.toJavaStringAt(term, 0), 0, 0));
        } else if(TermUtils.isAppl(term, "RDec", 2) && TermUtils.isStringAt(term, 0) && TermUtils
            .isListAt(term, 1)) {
            signatures.add(new StrategySignature(TermUtils.toJavaStringAt(term, 0),
                TermUtils.toListAt(term, 1).size(), 0));
        } else if(TermUtils.isAppl(term, "RDecT", 3) && TermUtils.isStringAt(term, 0) && TermUtils
            .isListAt(term, 1) && TermUtils.isListAt(term, 2)) {
            signatures.add(new StrategySignature(TermUtils.toJavaStringAt(term, 0),
                TermUtils.toListAt(term, 1).size(), TermUtils.toListAt(term, 2).size()));
        } else {
            for(IStrategoTerm child : term) {
                visit(child);
            }
        }
    }
}
