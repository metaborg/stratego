package strategolib.strategies;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.util.EqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import strategolib.terms.CompiledStrategyEqualityComparator;

public class internal_immutable_map_intersect_eq_2_1 extends Strategy {
    public static final internal_immutable_map_intersect_eq_2_1 instance = new internal_immutable_map_intersect_eq_2_1();

    /**
     * Stratego 2 type: {@code internal-immutable-map-intersect-eq :: (v * v -> v, v * v -> ?|ImmutableMapImplBlob) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy merge, Strategy compare, IStrategoTerm other) {
        return callStatic(context, current, merge, compare, other);
    }
    
    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy merge, Strategy compare, IStrategoTerm other) {
        return intersect(context, current, merge, other, new CompiledStrategyEqualityComparator(context, compare));
    }

    protected static IStrategoTerm intersect(Context context, IStrategoTerm current, Strategy merge, IStrategoTerm otherTerm,
        EqualityComparator<Object> cmp) {
        final ITermFactory factory = context.getFactory();
        final Map.Transient<IStrategoTerm, IStrategoTerm> intersection = CapsuleUtil.transientMap();
        final Map.Immutable<IStrategoTerm, IStrategoTerm> one = ((StrategoImmutableMap) current).backingMap;
        final Map.Immutable<IStrategoTerm, IStrategoTerm> other = ((StrategoImmutableMap) otherTerm).backingMap;
        for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> e : other.entrySet()) {
            if(one.containsKeyEquivalent(e.getKey(), cmp)) {
                final IStrategoTerm left = one.get(e.getKey());
                final IStrategoTerm right = e.getValue();
                current = merge.invoke(context, factory.makeTuple(left, right));
                if(current == null) {
                    return null;
                }
                intersection.__put(e.getKey(), current);
            }
        }

        return new StrategoImmutableMap(intersection.freeze());
    }
}
