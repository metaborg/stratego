package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import strategolib.terms.StrategoImmutableSet;
import strategolib.terms.StrategyEqualityComparator;

public class internal_immutable_set_intersect_eq_1_1 extends Strategy {
    public static internal_immutable_set_intersect_eq_1_1 instance = new internal_immutable_set_intersect_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy comp, IStrategoTerm otherTerm) {
        final Set.Immutable<IStrategoTerm> one = ((StrategoImmutableSet) current).backingSet;
        final Set.Transient<IStrategoTerm> other = ((StrategoImmutableSet) otherTerm).backingSet.asTransient();

        return 
            new StrategoImmutableSet(one.__retainAllEquivalent(other, new StrategyEqualityComparator(context, comp)));
    }
}
