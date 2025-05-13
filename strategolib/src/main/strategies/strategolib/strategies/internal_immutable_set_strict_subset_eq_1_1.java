package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import io.usethesource.capsule.util.EqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import strategolib.terms.CompiledStrategyEqualityComparator;

public class internal_immutable_set_strict_subset_eq_1_1 extends Strategy {
    public static final internal_immutable_set_strict_subset_eq_1_1 instance =
        new internal_immutable_set_strict_subset_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm otherTerm) {
        return callStatic(context, current, compare, otherTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy compare, IStrategoTerm otherTerm) {
        return strictSubset(context, current, otherTerm, new CompiledStrategyEqualityComparator(context, compare));
    }

    protected static IStrategoTerm strictSubset(Context context, IStrategoTerm current, IStrategoTerm otherTerm,
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
