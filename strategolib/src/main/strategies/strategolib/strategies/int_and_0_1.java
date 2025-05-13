package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class int_and_0_1 extends Strategy {
    public static final int_and_0_1 instance = new int_and_0_1();

    public static int operation(int left, int right) {
        return left & right;
    }

    /**
     * Stratego 2 type: {@code (|int) int -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm left, IStrategoTerm right) {
        return callStatic(context, left, right);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm left, IStrategoTerm right) {
        return context.getFactory().makeInt(operation(TermUtils.toJavaInt(left), TermUtils.toJavaInt(right)));
    }
}
