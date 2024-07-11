package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class getenv_0_0 extends Strategy {
    public static getenv_0_0 instance = new getenv_0_0();

    /**
     * Stratego 2 type: {@code getenv :: (|) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final String s = System.getenv(TermUtils.toJavaString(current));

        if(s == null)
            return null;

        return context.getFactory().makeString(s);
    }
}
