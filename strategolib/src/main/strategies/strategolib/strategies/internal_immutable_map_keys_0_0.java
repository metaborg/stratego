package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoImmutableMap;

public class internal_immutable_map_keys_0_0 extends Strategy {
    public static internal_immutable_map_keys_0_0 instance = new internal_immutable_map_keys_0_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map-keys :: (|) ImmutableMapImplBlob -> List(k)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;

        return context.getFactory().makeList(map.backingMap.keySet());
    }
}
