package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_setenv_0_2 extends Strategy {
    public static final internal_setenv_0_2 instance = new internal_setenv_0_2();

    /**
     * Stratego 2 type: {@code internal-setenv :: (|string * int) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm variableName, IStrategoTerm value, IStrategoTerm overwrite) {
        return callStatic(context, variableName, value, overwrite);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm variableName, IStrategoTerm value, IStrategoTerm overwrite) {
        context.getIOAgent().printError("'SSL_setenv' is not implementable in Java.");
        return null;
    }
}
