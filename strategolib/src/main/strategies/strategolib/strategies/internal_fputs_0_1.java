package strategolib.strategies;

import java.io.IOException;
import java.io.Writer;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_fputs_0_1 extends Strategy {
    public static final internal_fputs_0_1 instance = new internal_fputs_0_1();

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
        final Writer out = ioAgent.getWriter(TermUtils.toJavaInt(current));
        try {
            out.write(TermUtils.toJavaString(string));
        } catch(IOException e) {
            ioAgent.printError("SSL_fputs: could not put string (" + e.getMessage() + ")");
            return null;
        }
        return current;
    }
}
