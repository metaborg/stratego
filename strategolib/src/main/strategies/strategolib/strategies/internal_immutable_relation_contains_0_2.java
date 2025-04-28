package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_contains_0_2 extends Strategy {
    public static final internal_immutable_relation_contains_0_2 instance = new internal_immutable_relation_contains_0_2();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key, IStrategoTerm value) {
        return callStatic(context, current, key, value);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key, IStrategoTerm value) {
        final StrategoImmutableRelation relation = (StrategoImmutableRelation) current;

        if(relation.backingRelation.containsEntry(key, value)) {
            return current;
        } else {
            return null;
        }
    }
}
