package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import strategolib.terms.StrategyEqualityComparator;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_set_subtract_eq_1_1 extends Strategy {
    public static internal_immutable_set_subtract_eq_1_1 instance = new internal_immutable_set_subtract_eq_1_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy comp,
        IStrategoTerm otherTerm) {
        final Set.Immutable<IStrategoTerm> one = ((StrategoImmutableSet) current).backingSet;
        final Set.Immutable<IStrategoTerm> other = ((StrategoImmutableSet) otherTerm).backingSet;

        final Set.Immutable<IStrategoTerm> result =
            one.__removeAllEquivalent(other, new StrategyEqualityComparator(context, comp));
        return new StrategoImmutableSet(result);
    }
}
