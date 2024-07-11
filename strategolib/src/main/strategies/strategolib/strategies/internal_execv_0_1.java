package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_execv_0_1 extends Strategy {
    public static internal_execv_0_1 instance = new internal_execv_0_1();

    /**
     * Stratego 2 type: {@code internal-execv :: (|?) ? -> ?}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm file, IStrategoTerm argv) {
        context.getIOAgent().printError("'SSL_execv' is not implemented.");
        return null;
    }
}
