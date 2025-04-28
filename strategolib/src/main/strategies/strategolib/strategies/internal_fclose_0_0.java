package strategolib.strategies;

import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_fclose_0_0 extends Strategy {
    public static final internal_fclose_0_0 instance = new internal_fclose_0_0();

    /**
     * Stratego 2 type: {@code internal-fclose :: (|) string -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOAgent ioAgent = context.getIOAgent();
        try {
            if(ioAgent != null && ioAgent.closeRandomAccessFile(TermUtils.toJavaInt(current))) {
                return context.getFactory().makeTuple();
            }
        } catch(InterpreterException e) {
            // cannot occur in current implementation, but in case it does, fall through to strategy failure
        }
        return null;
    }
}
