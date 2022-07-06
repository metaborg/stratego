package strategolib.strategies;

import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoImmutableRelation;

import org.spoofax.interpreter.terms.IStrategoTerm;

import static strategolib.terms.StrategoImmutableRelation.subtract;

public class internal_immutable_relation_subtract_0_1 extends Strategy {
    public static internal_immutable_relation_subtract_0_1 instance = new internal_immutable_relation_subtract_0_1();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm otherTerm) {
        final StrategoImmutableRelation left = (StrategoImmutableRelation) current;
        final StrategoImmutableRelation right = (StrategoImmutableRelation) otherTerm;

        return subtract(left, right);
    }

}
