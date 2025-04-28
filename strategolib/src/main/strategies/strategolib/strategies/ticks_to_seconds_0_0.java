package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.SSL_times;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class ticks_to_seconds_0_0 extends Strategy {
    public static final ticks_to_seconds_0_0 instance = new ticks_to_seconds_0_0();

    /**
     * Stratego 2 type: {@code ticks-to-seconds :: (|) int -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();
        final int timeValue = TermUtils.toJavaInt(current);

        return factory.makeReal(timeValue / (double) SSL_times.TICKS_PER_SECOND);
    }
}
