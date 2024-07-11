package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class string_to_real_0_0 extends Strategy {
    public static string_to_real_0_0 instance = new string_to_real_0_0();

    /**
     * Stratego 2 type: {@code string-to-real :: (|) string -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();

        String s = TermUtils.toJavaString(current);
        // parse the real in the string
        try {
            return factory.makeReal(Double.parseDouble(s));
        } catch(NumberFormatException e1) {
            try {
                return factory.makeReal(Double.parseDouble(s.trim()));
            } catch(NumberFormatException e2) {
                return null;
            }
        }
    }
}
