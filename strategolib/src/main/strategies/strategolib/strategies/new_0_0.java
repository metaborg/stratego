package strategolib.strategies;

import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class new_0_0 extends Strategy {
    public static final new_0_0 instance = new new_0_0();

    private static final WeakHashMap<Context, AlphaCounter> countersPerContext = new WeakHashMap<>();

    /**
     * Stratego 2 type: {@code new :: (|) ? -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();

        synchronized(countersPerContext) {
            final AlphaCounter counter = countersPerContext.computeIfAbsent(context, k -> new AlphaCounter());

            String s;
            IStrategoString result;
            do {
                counter.increment();
                s = counter.alphaCount() + "_" + counter.count();
            } while((result = factory.tryMakeUniqueString(s)) == null);

            return result;
        }
    }

    private static final class AlphaCounter {
        private int alphaCounter = 'a';
        private int counter;

        public void increment() {
            alphaCounter++;
            if(alphaCounter > 'z') {
                alphaCounter = 'a';
                counter++;
                if(counter < 0) {
                    counter = 0;
                }
            }
        }

        /**
         * @return character count between 'a' and 'z'
         */
        public char alphaCount() {
            return (char) alphaCounter;
        }

        public int count() {
            return counter;
        }
    }
}
