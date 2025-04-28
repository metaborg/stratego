package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.BinaryRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_map_1_0 extends Strategy {
    public static final internal_immutable_relation_map_1_0 instance = new internal_immutable_relation_map_1_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy mapping) {
        return callStatic(context, current, mapping);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy mapping) {
        final ITermFactory f = context.getFactory();

        final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> relation =
            ((StrategoImmutableRelation) current).backingRelation;
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> resultRelation = BinaryRelation.Transient.of();
        for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> e : relation.entrySet()) {
            current = mapping.invoke(context, f.makeTuple(e.getKey(), e.getValue()));
            if(current == null) {
                return null;
            }
            if(!(TermUtils.isTuple(current) && current.getSubtermCount() == 2)) {
                return null;
            }
            final IStrategoTerm newKey = current.getSubterm(0);
            final IStrategoTerm newValue = current.getSubterm(1);
            resultRelation.__insert(newKey, newValue);
        }

        return new StrategoImmutableRelation(resultRelation.freeze());
    }
}
