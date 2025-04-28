package strategolib.strategies;

import static org.spoofax.interpreter.library.ssl.StrategoImmutableRelation.intersect;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_intersect_0_1 extends Strategy {
    public static final internal_immutable_relation_intersect_0_1 instance = new internal_immutable_relation_intersect_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        return callStatic(context, current, otherTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        final StrategoImmutableRelation one = (StrategoImmutableRelation) current;
        final StrategoImmutableRelation other = (StrategoImmutableRelation) otherTerm;

        return intersect(one, other);
    }

}
