package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class is_int_0_0 extends Strategy {
    public static is_int_0_0 instance = new is_int_0_0();

    /**
     * Stratego 2 type: {@code is-int :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return TermUtils.isInt(current) ? current : null;
    }
}
