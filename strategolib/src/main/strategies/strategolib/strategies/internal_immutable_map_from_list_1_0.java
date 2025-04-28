package strategolib.strategies;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Map;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;

public class internal_immutable_map_from_list_1_0 extends Strategy {
    public static final internal_immutable_map_from_list_1_0 instance = new internal_immutable_map_from_list_1_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map-from-list :: (k * k -> k|) List(k * v) -> ImmutableMapImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy merge) {
        return callStatic(context, current, merge);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy merge) {
        final ITermFactory f = context.getFactory();

        final IStrategoList list = (IStrategoList) current;
        final Map.Transient<IStrategoTerm, IStrategoTerm> map = CapsuleUtil.transientMap();
        for(IStrategoTerm t : list) {
            if(!TermUtils.isTuple(t, 2)) {
                return null;
            }
            final IStrategoTerm key = t.getSubterm(0);
            final IStrategoTerm value = t.getSubterm(1);
            if(map.containsKey(key)) {
                final IStrategoTerm oldValue = map.get(key);
                current = merge.invoke(context, f.makeTuple(oldValue, value));
                if(current == null) {
                    return null;
                }
                map.__put(key, current);
            } else {
                map.__put(key, value);
            }
        }

        return new StrategoImmutableMap(map.freeze());
    }
}
