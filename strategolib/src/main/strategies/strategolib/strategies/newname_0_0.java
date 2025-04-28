package strategolib.strategies;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class newname_0_0 extends Strategy {
    public static final newname_0_0 instance = new newname_0_0();

    private static final WeakHashMap<Context, WeakHashMap<String, AtomicInteger>> countersPerContext =
        new WeakHashMap<>();

    /**
     * Stratego 2 type: {@code newname :: (|) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        String prefix = TermUtils.toJavaString(current);

        // Intern to ensure that we get the same hard-reference to the prefix string, which will be 
        //   used as a key in the countersPerContext weak hashmap.
        prefix = prefix.intern();

        final ITermFactory factory = context.getFactory();

        synchronized(countersPerContext) {
            final WeakHashMap<String, AtomicInteger> counters =
                countersPerContext.computeIfAbsent(context, k -> new WeakHashMap<>());
            final AtomicInteger counter =
                counters.computeIfAbsent(prefix, k -> new AtomicInteger());

            String result;
            IStrategoTerm resultTerm;
            do {
                int counterValue = getNextValue(context.getIOAgent(), counter);
                result = prefix + counterValue;
                resultTerm = factory.tryMakeUniqueString(result);
            } while(resultTerm == null);

            return factory.replaceTerm(resultTerm, current);
        }
    }

    private static int getNextValue(IOAgent iOAgent, AtomicInteger counter) {
        int result;
        while(true) {
            result = counter.getAndIncrement();
            if(result >= 0) {
                break;
            } else if(counter.compareAndSet(result, 0)) {
                iOAgent.printError("SSL_newname: counter wrapped around");
                return 0;
            }
        }
        return result;
    }
}
