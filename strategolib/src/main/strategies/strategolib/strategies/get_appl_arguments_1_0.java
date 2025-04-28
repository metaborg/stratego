package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class get_appl_arguments_1_0 extends Strategy {
    public static final get_appl_arguments_1_0 instance = new get_appl_arguments_1_0();

    /**
     * Stratego 2 type: {@code get-appl-arguments :: (? -> a|) ? -> List(a)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s) {
        return callStatic(context, current, s);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s) {
        if(!TermUtils.isAppl(current) && !TermUtils.isTuple(current)) {
            return null;
        }
        final ITermFactory factory = context.getFactory();

        final IStrategoList.Builder b = factory.arrayListBuilder(current.getSubtermCount());
        for(IStrategoTerm arg : current) {
            final IStrategoTerm result = s.invoke(context, arg);
            if(result == null) {
                return null;
            }
            b.add(result);
        }
        return factory.makeList(b);
    }
}
