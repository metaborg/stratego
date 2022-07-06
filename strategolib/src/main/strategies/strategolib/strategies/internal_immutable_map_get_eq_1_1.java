package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import strategolib.terms.StrategoImmutableMap;
import strategolib.terms.StrategyEqualityComparator;

public class internal_immutable_map_get_eq_1_1 extends Strategy {
    public static internal_immutable_map_get_eq_1_1 instance = new internal_immutable_map_get_eq_1_1();

    /**
     * Stratego 2 type: {@code internal-immutable-map-get-eq :: (k * k -> ?|k) ImmutableMapImplBlob -> v}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key) {
        return get(current, key, new StrategyEqualityComparator(context, compare));
    }

    protected IStrategoTerm get(IStrategoTerm current, IStrategoTerm key, EqualityComparator<Object> cmp) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;

        current = map.backingMap.getEquivalent(key, cmp);
        if(current == null) {
            return null;
        }
        return current;
    }
}
