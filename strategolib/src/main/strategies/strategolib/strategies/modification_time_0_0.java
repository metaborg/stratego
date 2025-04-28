package strategolib.strategies;

import java.io.File;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class modification_time_0_0 extends Strategy {
    public static final modification_time_0_0 instance = new modification_time_0_0();

    /**
     * Stratego 2 type: {@code modification-time :: (|) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final String fn = TermUtils.toJavaString(current);

        final File f = context.getIOAgent().openFile(fn);
        if(f == null) {
            return null;
        }

        long result = f.lastModified() / 1000;
        return context.getFactory().makeInt((int) result);
    }
}
