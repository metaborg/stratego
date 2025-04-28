package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class issock_0_0 extends Strategy {
    public static final issock_0_0 instance = new issock_0_0();

    /**
     * Stratego 2 type: {@code issock :: (|) FileMode -> FileMode}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        if((TermUtils.toJavaInt(current) & filemode_0_0.S_IFSOCK) != 0) {
            return current;
        }
        return null;
    }
}
