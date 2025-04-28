package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class string_to_int_0_0 extends Strategy {
    public static final string_to_int_0_0 instance = new string_to_int_0_0();

    /**
     * Stratego 2 type: {@code string-to-int :: (|) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();

        String s = TermUtils.toJavaString(current);
        // parse the integer in the string
        try {
            return factory.makeInt(Integer.parseInt(s));
        } catch(NumberFormatException e1) {
            try {
                return factory.makeInt(Integer.parseInt(s.trim()));
            } catch(NumberFormatException e2) {
                return null;
            }
        }
    }
}
