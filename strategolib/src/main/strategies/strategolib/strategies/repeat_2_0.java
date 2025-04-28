package strategolib.strategies;

import java.util.HashSet;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

import strategolib.terms.StrategyRef;

public class repeat_2_0 extends Strategy {
    public static final repeat_2_0 instance = new repeat_2_0();

    private static final int INSANE_LOOP_COUNT = 100000;

    /**
     * Stratego 2 type: {@code repeat :: (a -> a, a -> b|) a -> b}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s1, Strategy s2) {
        return callStatic(context, current, s1, s2);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s1, Strategy s2) {
        IStrategoTerm result = current;
        IStrategoTerm next = s1.invoke(context, result);

        int count = 0;

        while(next != null) {
            if(++count > INSANE_LOOP_COUNT)
                return invokeSuspiciously(context, result, s1);
            result = next;
            next = s1.invoke(context, result);
        }

        return s2.invoke(context, result);
    }

    private static IStrategoTerm invokeSuspiciously(Context context, IStrategoTerm next, Strategy s) {
        final Set<IStrategoTerm> valuesSeen = new HashSet<>();
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
