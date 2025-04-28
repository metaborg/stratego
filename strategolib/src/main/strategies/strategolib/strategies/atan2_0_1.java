package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class atan2_0_1 extends Strategy {
    public static final atan2_0_1 instance = new atan2_0_1();

    /**
     * Stratego 2 type: {@code atan2 :: (|real) real -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm left, IStrategoTerm right) {
        return callStatic(context, left, right);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm left, IStrategoTerm right) {
        return context.getFactory().makeReal(Math.atan2(TermUtils.toJavaReal(left), TermUtils.toJavaReal(right)));
    }
}
