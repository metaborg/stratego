package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoImmutableRelation;

public class internal_immutable_relation_values_0_0 extends Strategy {
    public static internal_immutable_relation_values_0_0 instance = new internal_immutable_relation_values_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final StrategoImmutableRelation map = (StrategoImmutableRelation) current;

        return context.getFactory().makeList(map.backingRelation.values());
    }
}
