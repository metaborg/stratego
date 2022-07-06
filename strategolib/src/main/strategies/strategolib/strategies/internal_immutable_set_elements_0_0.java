package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoImmutableSet;

public class internal_immutable_set_elements_0_0 extends Strategy {
    public static internal_immutable_set_elements_0_0 instance = new internal_immutable_set_elements_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final StrategoImmutableSet set = (StrategoImmutableSet) current;

        return context.getFactory().makeList(set.backingSet);
    }
}
