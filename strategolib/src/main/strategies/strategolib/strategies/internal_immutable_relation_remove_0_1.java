package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.BinaryRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_remove_0_1 extends Strategy {
    public static final internal_immutable_relation_remove_0_1 instance = new internal_immutable_relation_remove_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        return callStatic(context, current, key);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key) {
        final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> relation =
            ((StrategoImmutableRelation) current).backingRelation;
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = relation.asTransient();
        result.__remove(key);

        return new StrategoImmutableRelation(result.freeze());
    }

}
