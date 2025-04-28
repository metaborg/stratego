package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class creat_0_0 extends Strategy {
    public static final creat_0_0 instance = new creat_0_0();

    /**
     * Stratego 2 type: {@code creat :: (|) string -> FileDescriptor}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        context.getIOAgent().printError("'SSL_creat' is not implemented.");
        return null;
    }
}
