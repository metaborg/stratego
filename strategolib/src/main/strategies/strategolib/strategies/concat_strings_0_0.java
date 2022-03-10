package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class concat_strings_0_0 extends Strategy {
    public static concat_strings_0_0 instance = new concat_strings_0_0();

    /**
     * Stratego 2 type: {@code concat-strings :: (|) List(string) -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final IStrategoList list = TermUtils.toList(current);
        final StringBuilder result = new StringBuilder(list.size());

        for(IStrategoTerm t : list) {
            result.append(TermUtils.toJavaString(t));
        }

        return context.getFactory().makeString(result.toString());
    }
}
