package strategolib.strategies;

import org.strategoxt.lang.Context;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class internal_immutable_set_strict_subset_0_1 extends internal_immutable_set_strict_subset_eq_1_1 {
    public static final internal_immutable_set_strict_subset_0_1 instance = new internal_immutable_set_strict_subset_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        return callStatic(context, current, otherTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        return strictSubset(context, current, otherTerm, Object::equals);
    }
}
