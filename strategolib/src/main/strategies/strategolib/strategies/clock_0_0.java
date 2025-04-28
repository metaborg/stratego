package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class clock_0_0 extends Strategy {
    public static final clock_0_0 instance = new clock_0_0();

    /**
     * Stratego 2 type: {@code clock :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt(
            times_0_0.getSystemTime().map(st -> (int) (st / 1000L)).orElse(-1));
    }
}
