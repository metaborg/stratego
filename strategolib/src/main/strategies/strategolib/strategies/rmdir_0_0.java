package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class rmdir_0_0 extends Strategy {
    public static final rmdir_0_0 instance = new rmdir_0_0();

    /**
     * Stratego 2 type: {@code rmdir :: (|) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        int result = context.getIOAgent().rmdir(TermUtils.toJavaString(current)) ? 0 : -1;
        return context.getFactory().makeInt(result);
    }
}
