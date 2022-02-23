package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.StrategoHashMap;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_hashtable_keys_fold_1_1 extends Strategy {
    public static internal_hashtable_keys_fold_1_1 instance = new internal_hashtable_keys_fold_1_1();

    /**
     * Stratego 2 type: {@code internal-hashtable-keys-fold :: ((|?) a -> a|a) HashtableImplBlob -> a}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s, IStrategoTerm acc) {
        final StrategoHashMap hashtable = (StrategoHashMap) current;

        for(IStrategoTerm key : hashtable.keySet()) {
            acc = s.invoke(context, key);
            if(acc == null) {
                return null;
            }
        }

        return acc;
    }
}
