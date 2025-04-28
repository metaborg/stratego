package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class dup2_0_1 extends Strategy {
    public static final dup2_0_1 instance = new dup2_0_1();

    /**
     * Stratego 2 type: {@code dup2 :: (|FileDescriptor) FileDescriptor -> FileDescriptor}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm fromFd, IStrategoTerm toFd) {
        return callStatic(context, fromFd, toFd);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm fromFd, IStrategoTerm toFd) {
        context.getIOAgent().printError("'SSL_dup2' is not implemented.");
        return null;
    }
}
