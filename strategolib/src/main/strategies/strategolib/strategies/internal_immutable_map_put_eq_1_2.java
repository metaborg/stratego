package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import strategolib.terms.CompiledStrategyEqualityComparator;

public class internal_immutable_map_put_eq_1_2 extends Strategy {
    public static final internal_immutable_map_put_eq_1_2 instance = new internal_immutable_map_put_eq_1_2();

     /**
     * Stratego 2 type: {@code internal-immutable-map-put-eq :: (k * k -> ?|k, v) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key, IStrategoTerm value) {
        return callStatic(context, current, compare, key, value);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key, IStrategoTerm value) {
        return put(current, key, value, new CompiledStrategyEqualityComparator(context, compare));
    }

    protected static IStrategoTerm put(IStrategoTerm current, IStrategoTerm key, IStrategoTerm value, EqualityComparator<Object> cmp) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;

        return new StrategoImmutableMap(map.backingMap.__putEquivalent(key, value, cmp));
    }
}
