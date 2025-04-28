package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class is_placeholder_0_0 extends Strategy {
    public static final is_placeholder_0_0 instance = new is_placeholder_0_0();

    /**
     * Stratego 2 type: {@code is-placeholder :: (|) ? -> Placeholder(?)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return current.getType() == TermType.PLACEHOLDER ? current : null;
    }
}
