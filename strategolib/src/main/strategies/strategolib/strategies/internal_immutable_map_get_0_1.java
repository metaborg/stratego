package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

public class internal_immutable_map_get_0_1 extends internal_immutable_map_get_eq_1_1 {
    public static internal_immutable_map_get_0_1 instance = new internal_immutable_map_get_0_1();

    /**
     * Stratego 2 type: {@code internal-immutable-map-get :: (|k) ImmutableMapImplBlob -> v}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        return get(current, key, Object::equals);
    }
}
