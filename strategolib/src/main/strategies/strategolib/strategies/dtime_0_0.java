package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class dtime_0_0 extends Strategy {
    public static dtime_0_0 instance = new dtime_0_0();

    /**
     * Stratego 2 type: {@code dtime :: (|) ? -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_dtime' is not implemented.");
        return null;
    }
}
