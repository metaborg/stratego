package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class int_0_0 extends Strategy {
    public static final int_0_0 instance = new int_0_0();

    /**
     * Stratego 2 type: {@code int :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
       if(TermUtils.isReal(current)) {
           return context.getFactory().makeInt((int) TermUtils.toJavaReal(current));
       }
       if(TermUtils.isInt(current)) {
           return current;
       }
       return null;
    }
}
