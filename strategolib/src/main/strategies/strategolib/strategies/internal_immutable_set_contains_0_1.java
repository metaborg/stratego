package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.SSL_immutable_set_contains_eq;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

public class internal_immutable_set_contains_0_1 extends internal_immutable_set_contains_eq_1_1 {
    public static internal_immutable_set_contains_0_1 instance = new internal_immutable_set_contains_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        return contains(context, current, key, Object::equals);
    }
}
