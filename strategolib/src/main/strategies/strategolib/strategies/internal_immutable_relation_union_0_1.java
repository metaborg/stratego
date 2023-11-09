package strategolib.strategies;

import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.spoofax.interpreter.library.ssl.StrategoImmutableRelation.union;

public class internal_immutable_relation_union_0_1 extends Strategy {
    public static internal_immutable_relation_union_0_1 instance = new internal_immutable_relation_union_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        final StrategoImmutableRelation one = (StrategoImmutableRelation) current;
        final StrategoImmutableRelation other = (StrategoImmutableRelation) otherTerm;

        return union(one, other);
    }

}
