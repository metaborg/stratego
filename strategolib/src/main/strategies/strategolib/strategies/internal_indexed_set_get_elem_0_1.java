package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoSet;

public class internal_indexed_set_get_elem_0_1 extends Strategy {
    public static final internal_indexed_set_get_elem_0_1 instance = new internal_indexed_set_get_elem_0_1();

    /**
     * Stratego 2 type: {@code internal-indexed-set-getElem :: (|int) IndexedSetImplBlob -> ?}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm elem) {
        return callStatic(context, current, elem);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm elem) {
        return ((StrategoSet) current).getElem(TermUtils.toJavaInt(elem));
    }
}
