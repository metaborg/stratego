package strategolib.strategies;

import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoInt;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class newterm_0_0 extends Strategy {
    public static final newterm_0_0 instance = new newterm_0_0();

    private static final WeakHashMap<Context, Integer> counterPerContext = new WeakHashMap<>();

    /**
     * Stratego 2 type: {@code newterm :: (|) ? -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        synchronized(counterPerContext) {
            final Integer counter = counterPerContext.compute(context, (k, i) -> i == null ? 0 : i + 1);

            return new UniqueValueTerm(counter);
        }
    }

    /**
     * This is a strange deprecated not-quite-integer that's in this case kept unique by the above strategy.
     * It comes from SRTS_newint_0_0, where the state was kept in the UniqueValueTerm class instead of the strategy.
     */
    @Deprecated
    public static final class UniqueValueTerm extends StrategoInt {
        private static final long serialVersionUID = 1L;

        public UniqueValueTerm(int value) {
            super(value);
        }

        @Override public int hashFunction() {
            // Always different from basic stratego int hash
            return super.hashFunction() + 1;
        }

        @Override public boolean doSlowMatch(IStrategoTerm second) {
            // match will return true on object equals and that is all the UniqueValueTerm does
            return false;
        }

        @Override public boolean isUniqueValueTerm() {
            return true;
        }
    }
}
