package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class make_placeholder_0_0 extends Strategy {
    public static final make_placeholder_0_0 instance = new make_placeholder_0_0();

    /**
     * Stratego 2 type: {@code make-placeholder :: (|) a -> Placeholder(a)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makePlaceholder(current);
    }
}
