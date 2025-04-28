package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class time_0_0 extends Strategy {
    public static final time_0_0 instance = new time_0_0();

    /**
     * Stratego 2 type: {@code time :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt((int) (System.currentTimeMillis() / 1000l));
    }
}
