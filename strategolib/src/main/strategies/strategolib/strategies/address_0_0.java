package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class address_0_0 extends Strategy {
    public static final address_0_0 instance = new address_0_0();

    /**
     * Stratego 2 type: {@code address :: (|) ? -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeString(Integer.toString(System.identityHashCode(current)));
    }
}
