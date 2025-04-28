package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_fdopen_0_1 extends Strategy {
    public static final internal_fdopen_0_1 instance = new internal_fdopen_0_1();

    /**
     * Stratego 2 type: {@code internal-fdopen :: (|string) FileDescriptor -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm permissionString) {
        return callStatic(context, current, permissionString);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm permissionString) {
        context.getIOAgent().printError("'SSL_fdopen' is not implemented.");
        return null;
    }
}
