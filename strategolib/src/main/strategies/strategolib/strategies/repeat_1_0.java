package strategolib.strategies;

import java.util.HashSet;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategyRef;

public class repeat_1_0 extends Strategy {
    public static repeat_1_0 instance = new repeat_1_0();

    private static final int INSANE_LOOP_COUNT = 100000;

    /**
     * Stratego 2 type: {@code repeat :: (a -> a|) a -> a}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s) {
        IStrategoTerm result = current;
        IStrategoTerm next = s.invoke(context, result);

        int count = 0;

        while(next != null) {
            if(++count > INSANE_LOOP_COUNT)
                return invokeSuspiciously(context, result, s);
            result = next;
            next = s.invoke(context, result);
        }

        return result;
    }

    private IStrategoTerm invokeSuspiciously(Context context, IStrategoTerm next, Strategy s) {
        final Set<IStrategoTerm> valuesSeen = new HashSet<IStrategoTerm>();
        IStrategoTerm result;
        do {
            result = next;
            next = s.invoke(context, result);
            if(!valuesSeen.add(next))
                throw new StrategoException("Infinite loop detected in repeat strategy");
        } while(next != null);
        return result;
    }
}
