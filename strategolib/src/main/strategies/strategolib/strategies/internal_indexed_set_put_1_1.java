package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.library.ssl.StrategoSet;

public class internal_indexed_set_put_1_1 extends Strategy {
    public static final internal_indexed_set_put_1_1 instance = new internal_indexed_set_put_1_1();

    /**
     * Stratego 2 type: {@code internal-indexed-set-put :: (int -> int|?) IndexedSetImplBlob -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s, IStrategoTerm elem) {
        return callStatic(context, current, s, elem);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s, IStrategoTerm elem) {
        final ITermFactory factory = context.getFactory();
        final StrategoSet set = (StrategoSet) current;
        if(set.containsKey(elem)) {
            return s.invoke(context, factory.makeInt(set.getIndex(elem)));
        }
        return factory.makeInt(set.put(elem));
    }
}
