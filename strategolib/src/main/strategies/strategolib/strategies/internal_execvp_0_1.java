package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_execvp_0_1 extends Strategy {
    public static final internal_execvp_0_1 instance = new internal_execvp_0_1();

    /**
     * Stratego 2 type: {@code internal-execvp :: (|?) ? -> ?}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm file, IStrategoTerm argv) {
        return callStatic(context, file, argv);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm file, IStrategoTerm argv) {
        context.getIOAgent().printError("'SSL_execvp' is not implemented.");
        return null;
    }
}
