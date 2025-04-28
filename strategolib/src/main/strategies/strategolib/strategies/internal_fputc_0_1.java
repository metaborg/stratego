package strategolib.strategies;

import java.io.IOException;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

public class internal_fputc_0_1 extends Strategy {
    public static final internal_fputc_0_1 instance = new internal_fputc_0_1();

    /**
     * Stratego 2 type: {@code internal-fputs :: (|string) StreamImplBlob -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm string) {
        return callStatic(context, current, string);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm string) {
        final IOAgent ioAgent = context.getIOAgent();
        if(ioAgent == null) {
            return null;
        }
        try {
            ioAgent.writeChar(TermUtils.toJavaInt(current), TermUtils.toJavaInt(string));
        } catch(IOException e) {
            throw new StrategoException("Exception in execution of primitive 'SSL_fputc'", e);
        }
        return current;
    }
}
