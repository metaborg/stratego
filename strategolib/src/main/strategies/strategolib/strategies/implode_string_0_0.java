package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class implode_string_0_0 extends Strategy {
    public static final implode_string_0_0 instance = new implode_string_0_0();

    /**
     * Stratego 2 type: {@code implode-string :: (|) List(Char) -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IStrategoList list = TermUtils.toList(current);
        final StringBuilder result = new StringBuilder(list.size());

        for(IStrategoTerm t : list) {
            result.appendCodePoint(TermUtils.toJavaInt(t));
        }

        return context.getFactory().makeString(result.toString());
    }
}
