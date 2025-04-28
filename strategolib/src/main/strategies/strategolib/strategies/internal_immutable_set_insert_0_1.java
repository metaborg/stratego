package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

public class internal_immutable_set_insert_0_1 extends internal_immutable_set_insert_eq_1_1 {
    public static final internal_immutable_set_insert_0_1 instance = new internal_immutable_set_insert_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm value) {
        return callStatic(context, current, value);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm value) {
        return insert(context, current, value, Object::equals);
    }
}
