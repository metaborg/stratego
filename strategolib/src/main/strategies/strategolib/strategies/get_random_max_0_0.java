package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_random_max_0_0 extends Strategy {
    public static final get_random_max_0_0 instance = new get_random_max_0_0();

    /**
     * SSL_RAND_MAX
     *
     * Stratego 2 type: {@code get-random-max :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt(set_random_seed_0_0.getRandomMax());
    }
}
