package strategolib.strategies;

import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

public class close_0_0 extends Strategy {
    public static final close_0_0 instance = new close_0_0();

    /**
     * Stratego 2 type: {@code close :: (|) FileDescriptor -> FileDescriptor}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        int fd = TermUtils.toJavaInt(current);
        try {
            return context.getIOAgent().closeRandomAccessFile(fd) ? current : null;
        } catch(InterpreterException e) {
            // Cannot actually be reached with current implementation of IOAgent#closeRandomAccessFile(int)
            throw new StrategoException("Exception in execution of primitive 'SSL_close'", e);
        }
    }
}
