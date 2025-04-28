package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class real_to_string_0_0 extends Strategy {
    public static final real_to_string_0_0 instance = new real_to_string_0_0();

    /**
     * Stratego 2 type: {@code real-to-string :: (|) real -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeString(String.format("%.17g", TermUtils.toJavaReal(current)));
    }
}
