package strategolib.strategies;

import io.usethesource.capsule.Set;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_immutable_map_keys_to_set_0_0 extends Strategy {
    public static final internal_immutable_map_keys_to_set_0_0 instance = new internal_immutable_map_keys_to_set_0_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map-keys-to-set :: (|) ImmutableMapImplBlob -> ImmutableSetImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;
        final Set.Transient<IStrategoTerm> set = CapsuleUtil.transientSet();
        set.__insertAll(map.backingMap.keySet());

        return new StrategoImmutableSet(set.freeze());
    }
}
