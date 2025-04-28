package strategolib.strategies;

import java.util.Iterator;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import io.usethesource.capsule.util.EqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import strategolib.terms.CompiledStrategyEqualityComparator;

public class internal_immutable_map_intersect_set_eq_1_1 extends Strategy {
    public static final internal_immutable_map_intersect_set_eq_1_1 instance = new internal_immutable_map_intersect_set_eq_1_1();

    /**
     * Stratego 2 type: {@code internal-immutable-map-intersect-set-eq :: (v * v -> ?|ImmutableSetImplBlob) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm other) {
        return callStatic(context, current, compare, other);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm other) {
        return intersect_set(current, other, new CompiledStrategyEqualityComparator(context, compare));
    }

    protected static IStrategoTerm intersect_set(IStrategoTerm current, IStrategoTerm otherTerm, EqualityComparator<Object> cmp) {
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
