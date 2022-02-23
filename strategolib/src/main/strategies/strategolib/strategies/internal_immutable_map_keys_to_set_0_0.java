package strategolib.strategies;

import io.usethesource.capsule.Set;
import strategolib.terms.StrategoImmutableMap;
import strategolib.terms.StrategoImmutableSet;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_immutable_map_keys_to_set_0_0 extends Strategy {
    public static internal_immutable_map_keys_to_set_0_0 instance = new internal_immutable_map_keys_to_set_0_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map-keys-to-set :: (|) ImmutableMapImplBlob -> ImmutableSetImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;
        final Set.Transient<IStrategoTerm> set = Set.Transient.of();
        set.__insertAll(map.backingMap.keySet());

        return new StrategoImmutableSet(set.freeze());
    }
}
