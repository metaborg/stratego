package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoSet;

public class internal_indexed_set_remove_0_1 extends Strategy {
    public static final internal_indexed_set_remove_0_1 instance = new internal_indexed_set_remove_0_1();

    /**
     * Stratego 2 type: {@code internal-indexed-set-remove :: (|?) IndexedSetImplBlob -> IndexedSetImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        return callStatic(context, current, key);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key) {
        ((StrategoSet) current).remove(key);
        return current;
    }
}
