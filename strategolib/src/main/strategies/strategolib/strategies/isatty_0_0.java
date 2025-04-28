package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class isatty_0_0 extends Strategy {
    public static final isatty_0_0 instance = new isatty_0_0();

    /**
     * Stratego 2 type: {@code isatty :: (|) FileDescriptor -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        // Java does not directly have access to POSIX isatty. We might depend on a library like
        //  Jansi or jnr-posix, to call out to C for isatty.

        // We use a hack here to give an (only partially correct!) answer for stdin/out/err. 
        //  System.console() gives back null if stdin or stdout are not "isatty"
        final int fd = TermUtils.toJavaInt(current);
        if(fd == IOAgent.CONST_STDIN || fd == IOAgent.CONST_STDOUT) {
            if(System.console() != null) {
                return context.getFactory().makeInt(1);
            }
        }
        context.getIOAgent().printError("'SSL_isatty' is not fully implemented.");
        return null;
    }
}
