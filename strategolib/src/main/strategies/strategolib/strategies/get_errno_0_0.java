package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_errno_0_0 extends Strategy {
    public static get_errno_0_0 instance = new get_errno_0_0();

    /**
     * Stratego 2 type: {@code get-errno :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_get_errno' is not implemented.");
        return null;
    }
}
