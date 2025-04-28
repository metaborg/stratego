package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoSet;

public class internal_indexed_set_create_0_1 extends Strategy {
    public static final internal_indexed_set_create_0_1 instance = new internal_indexed_set_create_0_1();

    /**
     * Stratego 2 type: {@code internal-indexed-set-create :: (|int) int -> IndexedSetImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm maxLoadTerm, IStrategoTerm initialSizeTerm) {
        return callStatic(context, maxLoadTerm, initialSizeTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm maxLoadTerm, IStrategoTerm initialSizeTerm) {
        return new StrategoSet(TermUtils.toJavaInt(initialSizeTerm), TermUtils.toJavaInt(maxLoadTerm));
    }
}
