package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class $S$T$D$O$U$T__$F$I$L$E$N$O_0_0 extends Strategy {
    public static final $S$T$D$O$U$T__$F$I$L$E$N$O_0_0 instance = new $S$T$D$O$U$T__$F$I$L$E$N$O_0_0();

    /**
     * Stratego 2 type: {@code STDOUT_FILENO :: (|) a -> FileDescriptor}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt(IOAgent.CONST_STDOUT);
    }
}
