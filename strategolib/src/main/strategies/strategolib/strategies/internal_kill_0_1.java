package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_kill_0_1 extends Strategy {
    public static final internal_kill_0_1 instance = new internal_kill_0_1();

    /**
     * Stratego 2 type: {@code internal-setenv :: (|string * int) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm pid, IStrategoTerm signal) {
        return callStatic(context, pid, signal);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm pid, IStrategoTerm signal) {
        context.getIOAgent().printError("'SSL_kill' is not implemented.");
        return null;
    }
}
