package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class $E$X$D$E$V_0_0 extends Strategy {
    public static final $E$X$D$E$V_0_0 instance = new $E$X$D$E$V_0_0();

    /**
     * Stratego 2 type: {@code EXDEV :: (|) ? -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        System.err.println("error: error number EXDEV is not available on this system.");
        return null;
    }
}
