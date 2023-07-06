package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class fork_0_0 extends Strategy {
    public static fork_0_0 instance = new fork_0_0();

    /**
     * Stratego 2 type: {@code fork :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_fork' is not implemented.");
        return null;
    }
}
