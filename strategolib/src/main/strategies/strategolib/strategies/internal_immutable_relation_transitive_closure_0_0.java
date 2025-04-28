package strategolib.strategies;

import static org.spoofax.interpreter.library.ssl.StrategoImmutableRelation.transitiveClosure;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_transitive_closure_0_0 extends Strategy {
    public static final internal_immutable_relation_transitive_closure_0_0 instance = new internal_immutable_relation_transitive_closure_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final StrategoImmutableRelation map = (StrategoImmutableRelation) current;

        return transitiveClosure(map);
    }

}
