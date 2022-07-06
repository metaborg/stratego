package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoSet;

/**
 * This is a copy of internal_indexed_set_reset_0_0
 */
public class internal_indexed_set_destroy_0_0 extends Strategy {
    public static internal_indexed_set_destroy_0_0 instance = new internal_indexed_set_destroy_0_0();

    /**
     * Stratego 2 type: {@code internal-indexed-set-destroy :: (|) IndexedSetImplBlob -> IndexedSetImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        ((StrategoSet) current).clear();
        return current;
    }
}
