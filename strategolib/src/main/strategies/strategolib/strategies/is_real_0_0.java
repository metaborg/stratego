package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class is_real_0_0 extends Strategy {
    public static final is_real_0_0 instance = new is_real_0_0();

    /**
     * Stratego 2 type: {@code is-real :: (|) ? -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return TermUtils.isReal(current) ? current : null;
    }
}
