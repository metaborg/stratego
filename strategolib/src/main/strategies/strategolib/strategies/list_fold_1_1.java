package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class list_fold_1_1 extends Strategy {
    public static final list_fold_1_1 instance = new list_fold_1_1();

    /**
     * Stratego 2 type: {@code list-fold(c * a -> c | c) :: List(a) -> c}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s, IStrategoTerm acc) {
        return callStatic(context, current, s, acc);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s, IStrategoTerm acc) {
        for (IStrategoTerm listItem : current) {
            acc = s.invoke(context, listItem, acc);
            if(acc == null) {
                return null;
            }
        }
        return acc;
    }
}
