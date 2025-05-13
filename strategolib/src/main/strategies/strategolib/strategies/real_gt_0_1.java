package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class real_gt_0_1 extends Strategy {
    public static final real_gt_0_1 instance = new real_gt_0_1();

    public static boolean operation(double left, double right) {
        return left > right;
    }

    /**
     * Stratego 2 type: {@code (|int) int -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm left, IStrategoTerm right) {
        return callStatic(context, left, right);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm left, IStrategoTerm right) {
        return operation(TermUtils.toJavaReal(left), TermUtils.toJavaReal(right)) ? left : null;
    }
}
