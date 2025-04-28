package strategolib.strategies;

import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import io.usethesource.capsule.BinaryRelation;
import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableRelation;

public class internal_immutable_map_to_relation_0_0 extends Strategy {
    public static final internal_immutable_map_to_relation_0_0 instance = new internal_immutable_map_to_relation_0_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map-to-relation :: (|) ImmutableMapImplBlob -> ImmutableRelationImplBlob}
     */
     @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final StrategoImmutableMap map = (StrategoImmutableMap) current;
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = BinaryRelation.Transient.of();

        for(Map.Entry<IStrategoTerm, IStrategoTerm> e : map.backingMap.entrySet()) {
            final IStrategoTerm key = e.getKey();
            final IStrategoTerm value = e.getValue();
            result.__insert(key, value);
        }

        return new StrategoImmutableRelation(result.freeze());
    }
}
