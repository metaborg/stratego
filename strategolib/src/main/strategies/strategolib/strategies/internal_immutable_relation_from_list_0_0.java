package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.BinaryRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_from_list_0_0 extends Strategy {
    public static final internal_immutable_relation_from_list_0_0 instance = new internal_immutable_relation_from_list_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IStrategoList list = (IStrategoList) current;
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> relation = BinaryRelation.Transient.of();
        for(IStrategoTerm t : list) {
            if(!(TermUtils.isTuple(t) && t.getSubtermCount() == 2)) {
                return null;
            }
            relation.__insert(t.getSubterm(0), t.getSubterm(1));
        }

        return new StrategoImmutableRelation(relation.freeze());
    }
}
