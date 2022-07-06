package strategolib.strategies;

import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoImmutableSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class internal_immutable_set_0_0 extends Strategy {
    public static internal_immutable_set_0_0 instance = new internal_immutable_set_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return new StrategoImmutableSet();
    }
}
