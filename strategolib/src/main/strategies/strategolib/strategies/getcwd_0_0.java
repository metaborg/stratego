package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class getcwd_0_0 extends Strategy {
    public static final getcwd_0_0 instance = new getcwd_0_0();

    /**
     * Stratego 2 type: {@code getcwd :: (|) ? -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOAgent ioAgent = context.getIOAgent();
        final String cwd = ioAgent.openFile(ioAgent.getWorkingDir()).getAbsolutePath();

        return context.getFactory().makeString(cwd);
    }
}
