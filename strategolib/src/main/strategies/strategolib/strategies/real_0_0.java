package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class real_0_0 extends Strategy {
    public static final real_0_0 instance = new real_0_0();

    /**
     * Stratego 2 type: {@code real :: (|) ? -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        if(TermUtils.isInt(current)) {
            return context.getFactory().makeReal(TermUtils.toJavaInt(current));
        }
       if(TermUtils.isReal(current)) {
           return current;
       }
       return null;
    }
}
