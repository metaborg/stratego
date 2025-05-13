package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class string_replace_0_2 extends Strategy {
    public static final string_replace_0_2 instance = new string_replace_0_2();

    /**
     * Stratego 2 type: {@code term-address-lt :: (|b) a -> a}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm beforeTerm, IStrategoTerm afterTerm) {
        return callStatic(context, current, beforeTerm, afterTerm);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm beforeTerm, IStrategoTerm afterTerm) {
        final String before = TermUtils.toJavaString(beforeTerm);
        if(before.isEmpty()) {
            return current;
        }
        final String after = TermUtils.toJavaString(afterTerm);
        final String input = TermUtils.toJavaString(current);
        final String result = input.replace(before, after);
        if(input.equals(result)) {
            return current;
        } else {
            return context.getFactory().makeString(result);
        }
    }
}
