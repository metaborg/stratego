package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoSet;

public class internal_indexed_set_reset_0_0 extends Strategy {
    public static final internal_indexed_set_reset_0_0 instance = new internal_indexed_set_reset_0_0();

    /**
     * Stratego 2 type: {@code internal-indexed-set-reset :: (|) IndexedSetImplBlob -> IndexedSetImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        ((StrategoSet) current).clear();
        return current;
    }
}
