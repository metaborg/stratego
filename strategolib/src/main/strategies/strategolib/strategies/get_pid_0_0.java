package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_pid_0_0 extends Strategy {
    public static final get_pid_0_0 instance = new get_pid_0_0();

    /**
     * Stratego 2 type: {@code get-pid :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_get_pid' is not implemented.");
        return null;
    }
}
