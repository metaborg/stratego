package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import strategolib.terms.CompiledStrategyEqualityComparator;

public class internal_immutable_map_remove_eq_1_1 extends Strategy {
    public static final internal_immutable_map_remove_eq_1_1 instance = new internal_immutable_map_remove_eq_1_1();

    /**
     * Stratego 2 type: {@code internal-immutable-map-remove-eq :: (k * k -> ?|k) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
     @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key) {
        return callStatic(context, current, compare, key);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key) {
        return remove(current, key, new CompiledStrategyEqualityComparator(context, compare));
    }

    protected static IStrategoTerm remove(IStrategoTerm current, IStrategoTerm key, EqualityComparator<Object> cmp) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;

        return new StrategoImmutableMap(map.backingMap.__removeEquivalent(key, cmp));
    }
}
