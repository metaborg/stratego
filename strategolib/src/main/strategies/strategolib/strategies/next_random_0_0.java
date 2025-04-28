package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class next_random_0_0 extends Strategy {
    public static final next_random_0_0 instance = new next_random_0_0();

    /**
     * SSL_rand
     * Note: Not implemented like SSL_rand in Java but more like the original C implementation
     *       This means there is once again the guarantee of the same pseudo-random numbers when no
     *           seed was set with {@code set-random-seed(|)}.
     *
     * Stratego 2 type: {@code get-random-max :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt(set_random_seed_0_0.instance.getNextRandomInt(context));
    }
}
