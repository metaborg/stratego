package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_immutable_map_union_1_1 extends internal_immutable_map_union_eq_2_1 {
    public static final internal_immutable_map_union_1_1 instance = new internal_immutable_map_union_1_1();
    

    /**
     * Stratego 2 type: {@code internal-immutable-map-union :: (v * v -> v|ImmutableMapImplBlob) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
     @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy merge, IStrategoTerm other) {
        return callStatic(context, current, merge, other);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy merge, IStrategoTerm other) {
        return union(context, current, merge, other, Object::equals);
    }
}
