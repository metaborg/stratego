package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.BinaryRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_to_list_0_0 extends Strategy {
    public static final internal_immutable_relation_to_list_0_0 instance = new internal_immutable_relation_to_list_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();

        final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> relation =
            ((StrategoImmutableRelation) current).backingRelation;
        final IStrategoTerm[] array =
            relation.tupleStream((k, v) -> factory.makeTuple(k, v)).toArray(IStrategoTerm[]::new);

        return factory.makeList(array);
    }
}
