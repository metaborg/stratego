package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Set;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

public class internal_immutable_relation_get_0_1 extends Strategy {
    public static final internal_immutable_relation_get_0_1 instance = new internal_immutable_relation_get_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        return callStatic(context, current, key);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key) {
        final StrategoImmutableRelation relation = (StrategoImmutableRelation) current;

        final Set.Immutable<IStrategoTerm> newCurrent = relation.backingRelation.get(key);
        return new StrategoImmutableSet(newCurrent);
    }

}
