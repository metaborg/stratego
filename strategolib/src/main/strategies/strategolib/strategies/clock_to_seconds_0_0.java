package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class clock_to_seconds_0_0 extends Strategy {
    public static final clock_to_seconds_0_0 instance = new clock_to_seconds_0_0();

    // in other words, clock returns a time span in microseconds
    public static final double CLOCKS_PER_SEC = 1_000_000;

    /**
     * Stratego 2 type: {@code clock-to-seconds :: (|) int -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeReal(TermUtils.toJavaInt(current) / CLOCKS_PER_SEC);
    }
}
