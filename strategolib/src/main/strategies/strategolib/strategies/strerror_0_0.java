package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class strerror_0_0 extends Strategy {
    public static final strerror_0_0 instance = new strerror_0_0();

    /**
     * Stratego 2 type: {@code strerror :: (|) int -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_strerror' is not implemented.");
        return null;
    }
}
