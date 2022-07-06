package strategolib.strategies;

import static strategolib.terms.StrategoImmutableRelation.reflTransClos;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoImmutableRelation;

public class internal_immutable_relation_reflexive_transitive_closure_0_0 extends Strategy {
    public static internal_immutable_relation_reflexive_transitive_closure_0_0 instance = new internal_immutable_relation_reflexive_transitive_closure_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final StrategoImmutableRelation map = (StrategoImmutableRelation) current;

        return new StrategoImmutableRelation(reflTransClos(map.backingRelation).freeze());
    }

}
