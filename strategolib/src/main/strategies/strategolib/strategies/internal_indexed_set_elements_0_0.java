package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategoSet;

public class internal_indexed_set_elements_0_0 extends Strategy {
    public static internal_indexed_set_elements_0_0 instance = new internal_indexed_set_elements_0_0();

    /**
     * Stratego 2 type: {@code internal-indexed-set-elements :: (|) IndexedSetImplBlob -> List(?)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return context.getFactory().makeList(((StrategoSet) current).keySet());
    }
}
