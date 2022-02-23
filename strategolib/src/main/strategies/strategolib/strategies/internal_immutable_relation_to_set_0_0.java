package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import strategolib.terms.StrategoImmutableRelation;
import strategolib.terms.StrategoImmutableSet;

public class internal_immutable_relation_to_set_0_0 extends Strategy {
    public static internal_immutable_relation_to_set_0_0 instance = new internal_immutable_relation_to_set_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final StrategoImmutableRelation relation = (StrategoImmutableRelation) current;
        final Set.Transient<IStrategoTerm> result = Set.Transient.of();
        for(IStrategoTerm pair : relation) {
            result.__insert(pair);
        }

        return new StrategoImmutableSet(result.freeze());
    }
}
