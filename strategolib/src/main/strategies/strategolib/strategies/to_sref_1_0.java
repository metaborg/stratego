package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategyRef;

public class to_sref_1_0 extends Strategy {
    public static final to_sref_1_0 instance = new to_sref_1_0();

    /**
     * Stratego 2 type: {@code to-sref(? -> ?) :: ? -> StrategyRef}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s) {
        return callStatic(context, current, s);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s) {
        return new StrategyRef(s);
    }
}
