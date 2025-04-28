package strategolib.strategies;

import java.io.IOException;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class chdir_0_0 extends Strategy {
    public static final chdir_0_0 instance = new chdir_0_0();

    /**
     * Stratego 2 type: {@code chdir :: (|) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOAgent io = context.getIOAgent();
        try {
            io.setWorkingDir(TermUtils.toJavaString(current));

            return context.getFactory().makeInt(0);
        } catch(IOException e) {
            return null;
        }
    }
}
