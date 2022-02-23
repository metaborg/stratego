package strategolib.strategies;

import java.util.Iterator;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import io.usethesource.capsule.util.EqualityComparator;
import strategolib.terms.StrategoImmutableMap;
import strategolib.terms.StrategoImmutableSet;
import strategolib.terms.StrategyEqualityComparator;

public class internal_immutable_map_intersect_set_eq_1_1 extends Strategy {
    public static internal_immutable_map_intersect_set_eq_1_1 instance = new internal_immutable_map_intersect_set_eq_1_1();

    /**
     * Stratego 2 type: {@code internal-immutable-map-intersect-set-eq :: (v * v -> ?|ImmutableSetImplBlob) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm other) {
        return intersect_set(current, other, new StrategyEqualityComparator(context, compare));
    }

    protected IStrategoTerm intersect_set(IStrategoTerm current, IStrategoTerm otherTerm, EqualityComparator<Object> cmp) {
        final Map.Transient<IStrategoTerm, IStrategoTerm> one =
            ((StrategoImmutableMap) current).backingMap.asTransient();
        final Set.Immutable<IStrategoTerm> other = ((StrategoImmutableSet) otherTerm).backingSet;
        for(Iterator<IStrategoTerm> iterator = one.keyIterator(); iterator.hasNext(); ) {
            IStrategoTerm key = iterator.next();
            if(!other.containsEquivalent(key, cmp)) {
                iterator.remove();
            }
        }

        return new StrategoImmutableMap(one.freeze());
    }
}
