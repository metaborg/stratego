package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class stacktrace_get_all_frame_names_0_0 extends Strategy {
    public static final stacktrace_get_all_frame_names_0_0 instance = new stacktrace_get_all_frame_names_0_0();

    /**
     * Stratego 2 type: {@code stacktrace-get-all-frame-names :: ? -> List(string)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();
        final IStrategoList.Builder b = factory.arrayListBuilder(context.getTraceDepth());
        for(String stackFrame : context.getTrace()) {
            b.add(factory.makeString(stackFrame));
        }
        return factory.makeList(b);
    }
}
