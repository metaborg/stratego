package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class real_to_string_0_1 extends Strategy {
    public static final real_to_string_0_1 instance = new real_to_string_0_1();

    /**
     * SSL_real_to_string_precision
     *
     * Stratego 2 type: {@code real-to-string :: (|) real -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm precision) {
        return callStatic(context, current, precision);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm precision) {
        final String format = "%." + TermUtils.toJavaInt(precision) + "f";
        return context.getFactory().makeString(String.format(format, TermUtils.toJavaReal(current)));
    }
}
