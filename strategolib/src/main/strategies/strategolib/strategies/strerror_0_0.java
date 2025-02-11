package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class strerror_0_0 extends Strategy {
    public static strerror_0_0 instance = new strerror_0_0();

    /**
     * Stratego 2 type: {@code strerror :: (|) int -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_strerror' is not implemented.");
        return null;
    }
}
