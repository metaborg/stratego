package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class strcat_0_1 extends Strategy {
    public static final strcat_0_1 instance = new strcat_0_1();

    /**
     * Stratego 2 type: {@code strcat :: (|string) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm left, IStrategoTerm right) {
        return callStatic(context, left, right);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm left, IStrategoTerm right) {
        return context.getFactory().makeString(TermUtils.toJavaString(left) + TermUtils.toJavaString(right));
    }
}
