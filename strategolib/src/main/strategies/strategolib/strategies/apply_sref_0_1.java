package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategyRef;

public class apply_sref_0_1 extends Strategy {
    public static final apply_sref_0_1 instance = new apply_sref_0_1();

    /**
     * Stratego 2 type: {@code apply-sref(|StrategyRef) :: ? -> ?}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm sref) {
        return callStatic(context, current, sref);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm sref) {
        return ((StrategyRef) sref).s.invoke(context, current);
    }
}
