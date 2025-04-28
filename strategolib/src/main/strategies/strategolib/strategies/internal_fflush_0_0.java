package strategolib.strategies;

import java.io.IOException;
import java.io.Writer;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_fflush_0_0 extends Strategy {
    public static final internal_fflush_0_0 instance = new internal_fflush_0_0();

    /**
     * Stratego 2 type: {@code internal-fclose :: (|) string -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOAgent ioAgent = context.getIOAgent();
        if(ioAgent == null) {
            return null;
        }
        final Writer writer = ioAgent.getWriter(TermUtils.toJavaInt(current));
        if(writer == null) {
            return null;
        }
        try {
            writer.flush();
        } catch(IOException e) {
            return null;
        }
        return current;
    }
}
