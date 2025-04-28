package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class stacktrace_get_current_frame_name_0_0 extends Strategy {
    public static final stacktrace_get_current_frame_name_0_0 instance = new stacktrace_get_current_frame_name_0_0();

    /**
     * Stratego 2 type: {@code stacktrace-get-current-frame-name :: ? -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();
        return factory.makeString(context.getTrace(true)[1]);
    }
}
