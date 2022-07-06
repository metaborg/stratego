package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class strlen_0_0 extends Strategy {
    public static strlen_0_0 instance = new strlen_0_0();

    /**
     * Stratego 2 type: {@code strlen :: (|) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt(TermUtils.toJavaString(current).length());
    }
}
