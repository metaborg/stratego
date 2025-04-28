package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;

public class exit_0_0 extends Strategy {
    public static final exit_0_0 instance = new exit_0_0();

    /**
     * Stratego 2 type: {@code exit :: (|) int -> a}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOAgent ioAgent = context.getIOAgent();
        if(ioAgent == null) {
            return null;
        }
        ioAgent.closeAllFiles();

        final int exitCode = TermUtils.toJavaInt(current);
        context.popOnExit(exitCode == 0);
        
        throw new StrategoExit(exitCode);
    }
}
