package strategolib.strategies;

import java.io.IOException;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_fopen_0_1 extends Strategy {
    public static internal_fopen_0_1 instance = new internal_fopen_0_1();

    /**
     * Stratego 2 type: {@code internal-fopen :: (|string) string -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm pathname, IStrategoTerm modeTerm) {
        return context.getFactory()
            .makeInt(call(context.getIOAgent(), TermUtils.toJavaString(pathname), TermUtils.toJavaString(modeTerm)));
    }

    protected Integer call(IOAgent ioAgent, String pathname, String mode) {
        if(ioAgent == null) {
            return null;
        }
        try {
            return ioAgent.openRandomAccessFile(pathname, mode);
        } catch(IOException e) {
            return null;
        }
    }
}
