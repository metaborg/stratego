package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class stdout_stream_0_0 extends Strategy {
    public static final stdout_stream_0_0 instance = new stdout_stream_0_0();

    /**
     * Stratego 2 type: {@code stdout-stream :: (|) ? -> Stream}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();
        return factory.makeAppl("Stream", factory.makeInt(IOAgent.CONST_STDOUT));
    }
}
