package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class remove_file_0_0 extends Strategy {
    public static final remove_file_0_0 instance = new remove_file_0_0();

    /**
     * (SSL_remove)
     * 
     * Stratego 2 type: {@code remove-file :: (|) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final String fn = TermUtils.toJavaString(current);

        final IOAgent ioAgent = context.getIOAgent();
        if(ioAgent != null && ioAgent.openFile(fn).delete()) {
            return current;
        }

        return null;
    }
}
