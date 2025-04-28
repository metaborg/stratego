package strategolib.strategies;

import java.util.Map.Entry;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Map;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_relation_to_map_1_0 extends Strategy {
    public static final internal_immutable_relation_to_map_1_0 instance = new internal_immutable_relation_to_map_1_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy merge) {
        return callStatic(context, current, merge);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy merge) {
        final ITermFactory f = context.getFactory();

        final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> relation =
            ((StrategoImmutableRelation) current).backingRelation;
        final Map.Transient<IStrategoTerm, IStrategoTerm> result = CapsuleUtil.transientMap();
        for(Entry<IStrategoTerm, IStrategoTerm> e : relation.entrySet()) {
            final IStrategoTerm key = e.getKey();
            final IStrategoTerm value = e.getValue();
            if(result.containsKey(key)) {
                final IStrategoTerm oldValue = result.get(key);
                current = merge.invoke(context, f.makeTuple(oldValue, value));
                if(current == null) {
                    return null;
                }
                result.__put(key, current);
            } else {
                result.__put(key, value);
            }
        }

        return new StrategoImmutableMap(result.freeze());
    }
}
