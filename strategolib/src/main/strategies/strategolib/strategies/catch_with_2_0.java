package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoErrorExit;
import org.strategoxt.lang.Strategy;

public class catch_with_2_0 extends Strategy {
    public static final catch_with_2_0 instance = new catch_with_2_0();

    /**
     * Stratego 2 type: {@code catch-with(a -> b, string * a * List(string) -> b) :: a -> b}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s, Strategy c) {
        return callStatic(context, current, s, c);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s, Strategy c) {
        final ITermFactory factory = context.getFactory();
        // Save trace before call
        final String[] savedTrace = context.getTrace();
        try {
            return s.invoke(context, current);
        } catch(StrategoErrorExit e) {
            final IStrategoString message = factory.makeString(e.getMessage() != null ? e.getMessage() : "");
            final IStrategoList failedTrace = e.getTrace() != null ? e.getTrace() : factory.makeList();
            // Restore trace after failed call
            context.setTrace(savedTrace);
            final IStrategoTerm exception = factory.makeTuple(message, current, failedTrace);
            final IStrategoTerm catchResult = c.invoke(context, exception);
            if(catchResult == null) {
                return current;
            } else {
                return catchResult;
            }
        }
    }
}
