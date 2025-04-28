package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class length_0_0 extends Strategy {
    public static final length_0_0 instance = new length_0_0();

    /**
     * Stratego 2 type: {@code length :: List(a) -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt(current.getSubtermCount());
    }
}
