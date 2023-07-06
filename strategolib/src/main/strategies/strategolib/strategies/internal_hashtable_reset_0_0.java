package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.StrategoHashMap;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_hashtable_reset_0_0 extends Strategy {
    public static internal_hashtable_reset_0_0 instance = new internal_hashtable_reset_0_0();

    /**
     * Stratego 2 type: {@code internal-hashtable-reset :: (|) HashtableImplBlob -> HashtableImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        ((StrategoHashMap) current).clear();
        return current;
    }
}
