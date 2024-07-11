package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_set_intersect_0_1 extends Strategy {
    public static internal_immutable_set_intersect_0_1 instance = new internal_immutable_set_intersect_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        final StrategoImmutableSet one = (StrategoImmutableSet) current;
        final StrategoImmutableSet other = (StrategoImmutableSet) otherTerm;

        return new StrategoImmutableSet(one.backingSet.intersect(other.backingSet));
    }
}
