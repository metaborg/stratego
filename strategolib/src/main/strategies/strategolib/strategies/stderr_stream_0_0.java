package strategolib.strategies;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class stderr_stream_0_0 extends Strategy {
    public static stderr_stream_0_0 instance = new stderr_stream_0_0();

    /**
     * Stratego 2 type: {@code stderr-stream :: (|) ? -> Stream}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();
        return factory.makeAppl("Stream", factory.makeInt(IOAgent.CONST_STDERR));
    }
}
