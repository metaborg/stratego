package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class islnk_0_0 extends Strategy {
    public static islnk_0_0 instance = new islnk_0_0();

    /**
     * Stratego 2 type: {@code islnk:: (|) FileMode -> FileMode}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        if((TermUtils.toJavaInt(current) & filemode_0_0.S_IFLINK) != 0) {
            return current;
        }
        return null;
    }
}
