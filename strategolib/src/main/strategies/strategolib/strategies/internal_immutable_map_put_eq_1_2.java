package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import strategolib.terms.StrategoImmutableMap;
import strategolib.terms.StrategyEqualityComparator;

public class internal_immutable_map_put_eq_1_2 extends Strategy {
    public static internal_immutable_map_put_eq_1_2 instance = new internal_immutable_map_put_eq_1_2();

     /**
     * Stratego 2 type: {@code internal-immutable-map-put-eq :: (k * k -> ?|k, v) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key,
        IStrategoTerm value) {
        return put(current, key, value, new StrategyEqualityComparator(context, compare));
    }

    protected IStrategoTerm put(IStrategoTerm current, IStrategoTerm key, IStrategoTerm value, EqualityComparator<Object> cmp) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;

        return new StrategoImmutableMap(map.backingMap.__putEquivalent(key, value, cmp));
    }
}
