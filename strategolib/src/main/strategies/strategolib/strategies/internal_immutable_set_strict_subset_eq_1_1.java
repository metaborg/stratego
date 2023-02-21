package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import io.usethesource.capsule.util.EqualityComparator;
import strategolib.terms.StrategyEqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_set_strict_subset_eq_1_1 extends Strategy {
    public static internal_immutable_set_strict_subset_eq_1_1 instance =
        new internal_immutable_set_strict_subset_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare,
        IStrategoTerm otherTerm) {
        return strictSubset(context, current, otherTerm, new StrategyEqualityComparator(context, compare));
    }

    protected IStrategoTerm strictSubset(Context context, IStrategoTerm current, IStrategoTerm otherTerm,
        EqualityComparator<Object> cmp) {
        final Set.Immutable<IStrategoTerm> left = ((StrategoImmutableSet) current).backingSet;
        final Set.Immutable<IStrategoTerm> right = ((StrategoImmutableSet) otherTerm).backingSet;
        if(left.size() >= right.size()) {
            return null;
        }

        for(IStrategoTerm elem : left) {
            if(!right.containsEquivalent(elem, cmp)) {
                return null;
            }
        }

        return current;
    }
}
