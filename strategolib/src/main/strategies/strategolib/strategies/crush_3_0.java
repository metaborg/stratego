package strategolib.strategies;

import java.util.ListIterator;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class crush_3_0 extends Strategy {
    public static final crush_3_0 instance = new crush_3_0();

    /**
     * Stratego 2 type: {@code crush :: (List(d) -> b, c * b -> b, ? -> c|) ? -> b}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy nul, Strategy sum, Strategy s) {
        return callStatic(context, current, nul, sum, s);
    }
    
    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy nul, Strategy sum, Strategy s) {
        final ITermFactory factory = context.getFactory();

        IStrategoTerm result = nul.invoke(context, factory.makeList());
        if(result == null) {
            return null;
        }

        for(ListIterator<IStrategoTerm> iterator =
            current.getSubterms().listIterator(current.getSubtermCount()); iterator.hasPrevious();) {
            IStrategoTerm subterm = iterator.previous();

            final IStrategoTerm subtermResult = s.invoke(context, subterm);
            if(subtermResult == null) {
                return null;
            }
            // UNDONE: factory.replaceTerm(subtermResult, subterm);

            result = sum.invoke(context, factory.makeTuple(subtermResult, result));
            if(result == null) {
                return null;
            }
        }

        return result;
    }
}
