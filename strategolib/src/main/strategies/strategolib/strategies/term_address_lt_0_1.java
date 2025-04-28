package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class term_address_lt_0_1 extends Strategy {
    public static final term_address_lt_0_1 instance = new term_address_lt_0_1();

    /**
     * Stratego 2 type: {@code term-address-lt :: (|b) a -> a}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm other) {
        return callStatic(context, current, other);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm other) {
        if(System.identityHashCode(current) < System.identityHashCode(other)) {
            return current;
        } else {
            return null;
        }
    }
}
