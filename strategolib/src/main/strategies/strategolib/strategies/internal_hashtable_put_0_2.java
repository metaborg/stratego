package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.StrategoHashMap;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_hashtable_put_0_2 extends Strategy {
    public static final internal_hashtable_put_0_2 instance = new internal_hashtable_put_0_2();

    /**
     * Stratego 2 type: {@code internal-hashtable-put :: (|?, ?) HashtableImplBlob -> HashtableImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm key, IStrategoTerm value) {
        return callStatic(context, current, key, value);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm key, IStrategoTerm value) {
        ((StrategoHashMap) current).put(key, value);
        return current;
    }
}
