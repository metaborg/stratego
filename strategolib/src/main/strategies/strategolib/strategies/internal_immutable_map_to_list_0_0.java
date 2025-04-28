package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.Map;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;

public class internal_immutable_map_to_list_0_0 extends Strategy {
    public static final internal_immutable_map_to_list_0_0 instance = new internal_immutable_map_to_list_0_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map-to-list :: (|) ImmutableMapImplBlob -> List(k * v)}
     */
     @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();

        final Map.Immutable<IStrategoTerm, IStrategoTerm> map = ((StrategoImmutableMap) current).backingMap;
        final IStrategoTerm[] array = new IStrategoTerm[map.size()];
        int i = 0;
        for(java.util.Map.Entry<IStrategoTerm, IStrategoTerm> e : map.entrySet()) {
            array[i] = factory.makeTuple(e.getKey(), e.getValue());
            i += 1;
        }

        return factory.makeList(array);
    }
}
