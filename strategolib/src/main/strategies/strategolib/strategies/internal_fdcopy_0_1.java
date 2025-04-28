package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_fdcopy_0_1 extends Strategy {
    public static final internal_fdcopy_0_1 instance = new internal_fdcopy_0_1();

    /**
     * Stratego 2 type: {@code internal-fdcopy :: (|FileDescriptor) FileDescriptor -> ?}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm fdIn, IStrategoTerm fdOut) {
        return callStatic(context, fdIn, fdOut);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm fdIn, IStrategoTerm fdOut) {
        context.getIOAgent().printError("'SSL_fdcopy' is not implementable in Java.");
        return null;
    }
}
