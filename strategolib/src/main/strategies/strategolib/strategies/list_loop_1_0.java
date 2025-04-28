package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class list_loop_1_0 extends Strategy {
    public static final list_loop_1_0 instance = new list_loop_1_0();

    /**
     * Stratego 2 type: {@code list-loop(a -> b) :: List(a) -> List(a)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s) {
        return callStatic(context, current, s);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s) {
        for(IStrategoTerm listItem : current) {
            if(s.invoke(context, listItem) == null) {
                return null;
            }
        }

        return current;
    }
}
