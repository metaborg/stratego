package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

public class internal_immutable_set_remove_0_1 extends internal_immutable_set_remove_eq_1_1 {
    public static final internal_immutable_set_remove_0_1 instance = new internal_immutable_set_remove_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        return callStatic(context, current, key);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key) {
        return remove(context, current, key, Object::equals);
    }
}
