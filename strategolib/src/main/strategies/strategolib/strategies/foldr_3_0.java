package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class foldr_3_0 extends crush_3_0 {
    public static final foldr_3_0 instance = new foldr_3_0();

    /**
     * Copy of crush, with more accurate type for input and third strategy argument.
     *
     * Stratego 2 type: {@code foldr :: (List(d) -> b, c * b -> b, a -> c|) List(a) -> b}
     */


    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy nul, Strategy sum, Strategy s) {
        return crush_3_0.callStatic(context, current, nul, sum, s);
    }
}
