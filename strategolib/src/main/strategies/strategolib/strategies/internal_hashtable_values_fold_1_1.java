package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.StrategoHashMap;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_hashtable_values_fold_1_1 extends Strategy {
    public static final internal_hashtable_values_fold_1_1 instance = new internal_hashtable_values_fold_1_1();

    /**
     * Stratego 2 type: {@code internal-hashtable-values-fold :: ((|?) a -> a|a) HashtableImplBlob -> a}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s, IStrategoTerm acc) {
        return callStatic(context, current, s, acc);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s, IStrategoTerm acc) {
        final StrategoHashMap hashtable = (StrategoHashMap) current;

        for(IStrategoTerm value : hashtable.values()) {
            acc = s.invoke(context, acc, value);
            if(acc == null) {
                return null;
            }
        }

        return acc;
    }
}
