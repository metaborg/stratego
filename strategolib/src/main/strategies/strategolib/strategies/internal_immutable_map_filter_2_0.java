package strategolib.strategies;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Map;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;

public class internal_immutable_map_filter_2_0 extends Strategy {
    public static final internal_immutable_map_filter_2_0 instance = new internal_immutable_map_filter_2_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map-filter :: (k1 * v1 -> k2 * v2, k2 * (v2 * v2) -> v2|) ImmutableMapImplBlob -> ImmutableMapImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy mapping, Strategy merge) {
        return callStatic(context, current, mapping, merge);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy mapping, Strategy merge) {
        final ITermFactory f = context.getFactory();

        final Map.Immutable<IStrategoTerm, IStrategoTerm> map = ((StrategoImmutableMap) current).backingMap;
        final Map.Transient<IStrategoTerm, IStrategoTerm> resultMap = CapsuleUtil.transientMap();
        for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> e : map.entrySet()) {
            current = mapping.invoke(context, f.makeTuple(e.getKey(), e.getValue()));
            if(current == null) {
                continue;
            }
            if(!TermUtils.isTuple(current, 2)) {
                return null;
            }
            final IStrategoTerm newKey = current.getSubterm(0);
            final IStrategoTerm newValue = current.getSubterm(1);
            if(resultMap.containsKey(newKey)) {
                final IStrategoTerm oldValue = resultMap.get(newKey);
                current = merge.invoke(context, f.makeTuple(newKey, f.makeTuple(oldValue, newValue)));
                if(current == null) {
                    return null;
                }
                resultMap.__put(newKey, current);
            } else {
                resultMap.__put(newKey, newValue);
            }
        }

        return new StrategoImmutableMap(resultMap.freeze());
    }
}
