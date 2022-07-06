package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class waitpid_0_0 extends Strategy {
    public static waitpid_0_0 instance = new waitpid_0_0();

    /**
     * Stratego 2 type: {@code waitpid :: (|) int -> WaitStatus}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_waitpid' is not implemented.");
        return null;
    }
}
