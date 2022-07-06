package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.util.EqualityComparator;
import strategolib.terms.StrategoImmutableSet;
import strategolib.terms.StrategyEqualityComparator;

public class internal_immutable_set_contains_eq_1_1 extends Strategy {
    public static internal_immutable_set_contains_eq_1_1 instance = new internal_immutable_set_contains_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm key) {
        return contains(context, current, key, new StrategyEqualityComparator(context, compare));
    }

    protected IStrategoTerm contains(Context context, IStrategoTerm current, IStrategoTerm key, EqualityComparator<Object> cmp) {
        final StrategoImmutableSet set = (StrategoImmutableSet) current;

        if(set.backingSet.containsEquivalent(key, cmp)) {
            return current;
        } else {
            return null;
        }
    }
}
