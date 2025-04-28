package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class isfifo_0_0 extends Strategy {
    public static final isfifo_0_0 instance = new isfifo_0_0();

    /**
     * Stratego 2 type: {@code isfifo :: (|) FileMode -> FileMode}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        if((TermUtils.toJavaInt(current) & filemode_0_0.S_IFFIFO) != 0) {
            return current;
        }
        return null;
    }
}
