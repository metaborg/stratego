package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.StrategoHashMap;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_hashtable_remove_0_1 extends Strategy {
    public static final internal_hashtable_remove_0_1 instance = new internal_hashtable_remove_0_1();

    /**
     * Stratego 2 type: {@code internal-hashtable-remove :: (|?) HashtableImplBlob -> HashtableImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key) {
        return callStatic(context, current, key);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key) {
        ((StrategoHashMap) current).remove(key);
        return current;
    }
}
