package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import strategolib.terms.StrategoImmutableRelation;
import strategolib.terms.StrategoImmutableSet;

public class internal_immutable_relation_get_0_1 extends Strategy {
    public static internal_immutable_relation_get_0_1 instance = new internal_immutable_relation_get_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        final StrategoImmutableRelation relation = (StrategoImmutableRelation) current;

        final Set.Immutable<IStrategoTerm> newCurrent = relation.backingRelation.get(key);
        return new StrategoImmutableSet(newCurrent);
    }

}
