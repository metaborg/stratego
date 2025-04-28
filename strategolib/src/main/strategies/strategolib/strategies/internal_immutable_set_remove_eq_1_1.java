package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import strategolib.terms.CompiledStrategyEqualityComparator;

public class internal_immutable_set_remove_eq_1_1 extends Strategy {
    public static final internal_immutable_set_remove_eq_1_1 instance = new internal_immutable_set_remove_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key) {
        return callStatic(context, current, compare, key);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key) {
        return remove(context, current, key, new CompiledStrategyEqualityComparator(context, compare));
    }

    protected static IStrategoTerm remove(Context context, IStrategoTerm current, IStrategoTerm key,
        EqualityComparator<Object> cmp) {
        final StrategoImmutableSet set = (StrategoImmutableSet) current;

        return new StrategoImmutableSet(set.backingSet.__removeEquivalent(key, cmp));
    }
}
