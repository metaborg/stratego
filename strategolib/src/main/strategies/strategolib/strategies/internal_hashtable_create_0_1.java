package strategolib.strategies;

import org.spoofax.interpreter.library.ssl.StrategoHashMap;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_hashtable_create_0_1 extends Strategy {
    public static final internal_hashtable_create_0_1 instance = new internal_hashtable_create_0_1();

    /**
     * Stratego 2 type: {@code internal-hashtable-create :: (|int) int -> HashtableImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm maxLoadTerm, IStrategoTerm initialSizeTerm) {
        return callStatic(context, maxLoadTerm, initialSizeTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm maxLoadTerm, IStrategoTerm initialSizeTerm) {
        return new StrategoHashMap(TermUtils.toJavaInt(initialSizeTerm), TermUtils.toJavaInt(maxLoadTerm));
    }
}
