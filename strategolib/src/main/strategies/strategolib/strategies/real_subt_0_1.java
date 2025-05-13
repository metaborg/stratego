package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class real_subt_0_1 extends Strategy {
    public static final real_subt_0_1 instance = new real_subt_0_1();

    public static double operation(double left, double right) {
        return left - right;
    }

    /**
     * Stratego 2 type: {@code (|real) real -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm left, IStrategoTerm right) {
        return callStatic(context, left, right);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm left, IStrategoTerm right) {
        return context.getFactory().makeReal(operation(TermUtils.toJavaReal(left), TermUtils.toJavaReal(right)));
    }
}
