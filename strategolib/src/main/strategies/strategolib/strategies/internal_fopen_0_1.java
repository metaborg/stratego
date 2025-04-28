package strategolib.strategies;

import java.io.IOException;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_fopen_0_1 extends Strategy {
    public static final internal_fopen_0_1 instance = new internal_fopen_0_1();

    /**
     * Stratego 2 type: {@code internal-fopen :: (|string) string -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm pathname, IStrategoTerm modeTerm) {
        return callStatic(context, pathname, modeTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm pathname, IStrategoTerm modeTerm) {
        final IOAgent ioAgent = context.getIOAgent();
        if(ioAgent == null) {
            return null;
        }

        final ITermFactory factory = context.getFactory();

        final String path = TermUtils.toJavaString(pathname);
        final String mode = TermUtils.toJavaString(modeTerm);

        try {
            final int result = ioAgent.openRandomAccessFile(path, mode);
            return factory.makeInt(result);
        } catch(IOException e) {
            return null;
        }
    }

}
