package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class $S$T$D$E$R$R_$F$I$L$E$N$O_0_0 extends Strategy {
    public static $S$T$D$E$R$R_$F$I$L$E$N$O_0_0 instance = new $S$T$D$E$R$R_$F$I$L$E$N$O_0_0();

    /**
     * Stratego 2 type: {@code STDERR_FILENO :: (|) a -> FileDescriptor}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return context.getFactory().makeInt(IOAgent.CONST_STDERR);
    }
}
