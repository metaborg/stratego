package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class islnk_0_0 extends Strategy {
    public static final islnk_0_0 instance = new islnk_0_0();

    /**
     * Stratego 2 type: {@code islnk:: (|) FileMode -> FileMode}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        if((TermUtils.toJavaInt(current) & filemode_0_0.S_IFLINK) != 0) {
            return current;
        }
        return null;
    }
}
