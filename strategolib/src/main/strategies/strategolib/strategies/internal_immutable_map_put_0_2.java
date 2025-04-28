package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

public class internal_immutable_map_put_0_2 extends internal_immutable_map_put_eq_1_2 {
    public static final internal_immutable_map_put_0_2 instance = new internal_immutable_map_put_0_2();

    /**
     * Stratego 2 type: {@code internal-immutable-map-put :: (|k, v) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
     @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key, IStrategoTerm value) {
        return callStatic(context, current, key, value);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key, IStrategoTerm value) {
        return put(current, key, value, Object::equals);
    }
}
