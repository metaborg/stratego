package strategolib.strategies;

import java.io.IOException;
import java.io.Reader;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

public class fgetc_0_0 extends Strategy {
    public static final fgetc_0_0 instance = new fgetc_0_0();

    /**
     * Stratego 2 type: {@code fgetc :: (|) Stream -> Char}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOAgent ioAgent = context.getIOAgent();

        if(ioAgent == null) {
            return null;
        }

        final Reader in = ioAgent.getReader(TermUtils.toJavaInt(current.getSubterm(0)));
        int r = -1;

        try {
            r = in.read();
        } catch(IOException e) {
            throw new StrategoException("Exception in execution of primitive 'SSL_fgetc'", e);
        }

        if(r == -1) {
            return null;
        }

        return context.getFactory().makeInt(r);
    }
}
