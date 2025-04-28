package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class explode_term_0_0 extends Strategy {
    public static final explode_term_0_0 instance = new explode_term_0_0();

    /**
     * Stratego 2 type: {@code explode-term :: (|) ? -> ? * List(?)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IStrategoTerm cons = get_constructor_0_0.instance.invoke(context, current);
        final IStrategoTerm args = get_arguments_0_0.instance.invoke(context, current);
        return context.getFactory().makeTuple(cons, args);
    }
}
