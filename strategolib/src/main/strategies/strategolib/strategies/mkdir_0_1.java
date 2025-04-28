package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class mkdir_0_1 extends Strategy {
    public static final mkdir_0_1 instance = new mkdir_0_1();

    /**
     * Stratego 2 type: {@code mkdir :: (|List(AccessPermission)) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm pathname, IStrategoTerm mode) {
        return callStatic(context, pathname, mode);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm pathname, IStrategoTerm mode) {
        int result = context.getIOAgent().mkdir(TermUtils.toJavaString(pathname)) ? 0 : -1;

        /* access parameter is ignored in C version
        AbstractPrimitive access = op.get("SSL_access");

        if (result == 0) { // Set access rights
            result = access.call(env, svars, tvars) ? 0 : -1;
        }
        */

        return context.getFactory().makeInt(result);
    }
}
