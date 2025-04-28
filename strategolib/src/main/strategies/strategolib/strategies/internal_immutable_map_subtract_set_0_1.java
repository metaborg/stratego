package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

public class internal_immutable_map_subtract_set_0_1 extends internal_immutable_map_subtract_set_eq_1_1 {
    public static final internal_immutable_map_subtract_set_0_1 instance = new internal_immutable_map_subtract_set_0_1();


    /**
     * Stratego 2 type: {@code internal-immutable-map-subtract-set :: (|ImmutableSetImplBlob) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
     @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm other) {
        return callStatic(context, current, other);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm other) {
        return subtract(current, other, Object::equals);
    }
}
