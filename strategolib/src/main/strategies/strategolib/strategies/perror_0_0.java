package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class perror_0_0 extends Strategy {
    public static final perror_0_0 instance = new perror_0_0();

    /**
     * Stratego 2 type: {@code perror :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final String message = TermUtils.isString(current) ? TermUtils.toJavaString(current)
            : "(no details on this error; perror not supported)";

        System.err.println("ERROR: " + message);
        return current;
    }
}
