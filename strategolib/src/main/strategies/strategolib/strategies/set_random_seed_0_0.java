package strategolib.strategies;

import java.util.Random;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class set_random_seed_0_0 extends Strategy {
    public static final set_random_seed_0_0 instance = new set_random_seed_0_0();

    private static final WeakHashMap<Context, Random> randomPerContext = new WeakHashMap<>();

    /**
     * SSL_srand
     *
     * Stratego 2 type: {@code set-random-seed :: (|) int -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        synchronized(randomPerContext) {
            randomPerContext.put(context, new Random(TermUtils.toJavaInt(current)));
            return current;
        }
    }

    public int getNextRandomInt(Context context) {
        synchronized(randomPerContext) {
            final Random random = randomPerContext.computeIfAbsent(context, k -> new Random(1));
            return random.nextInt();
        }
    }

    public static int getRandomMax() {
        return Integer.MAX_VALUE;
    }
}
