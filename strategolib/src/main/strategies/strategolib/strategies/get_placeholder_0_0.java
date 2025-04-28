package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoPlaceholder;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_placeholder_0_0 extends Strategy {
    public static final get_placeholder_0_0 instance = new get_placeholder_0_0();

    /**
     * Stratego 2 type: {@code get-placeholder :: (|) Placeholder(a) -> a}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        if (current.getType() == TermType.PLACEHOLDER) {
            return ((IStrategoPlaceholder) current).getTemplate();
        } else {
            return null;
        }
    }
}
