package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoSet;

public class internal_indexed_set_get_index_0_1 extends Strategy {
    public static internal_indexed_set_get_index_0_1 instance = new internal_indexed_set_get_index_0_1();

    /**
     * Stratego 2 type: {@code internal-indexed-set-getIndex :: (|?) IndexedSetImplBlob -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        final int index = ((StrategoSet) current).getIndex(key);
        if(index == -1) {
            return null;
        }
        return context.getFactory().makeInt(index);
    }
}
