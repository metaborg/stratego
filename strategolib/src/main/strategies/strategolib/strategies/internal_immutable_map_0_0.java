package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;

public class internal_immutable_map_0_0 extends Strategy {
    public static final internal_immutable_map_0_0 instance = new internal_immutable_map_0_0();

    /**
     * Stratego 2 type: {@code internal-immutable-map :: (|) ? -> ImmutableMapImplBlob}
     */
     @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return new StrategoImmutableMap();
    }
}
