package strategolib.strategies;

import static org.strategoxt.lang.Term.checkListTail;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class at_end_1_0 extends Strategy {
    public static final at_end_1_0 instance = new at_end_1_0();

    /**
     * Stratego 2 type: {@code at-end :: (List(b) -> List(a)|) List(a) -> List(a)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy s) {
        return callStatic(context, current, s);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, Strategy s) {
        IStrategoList list = TermUtils.toList(current);
        IStrategoTerm[] listItems = new IStrategoTerm[list.size()];

        for(int i = 0; i < listItems.length; i++) {
            if(!list.getAnnotations().isEmpty()) {
                final IStrategoList tail = atEndMaintainAnnos(context, list, s);
                if(tail == null) {
                    return null;
                }
                return concat(context.getFactory(), listItems, i, tail);
            }
            listItems[i] = list.head();
            list = list.tail();
        }

        final IStrategoTerm tail = s.invoke(context, context.getFactory().makeList());
        if(tail == null || checkListTail(tail) == null) {
            return null;
        }
        return concat(context.getFactory(), listItems, listItems.length, TermUtils.toList(tail));
    }

    private static IStrategoList concat(ITermFactory factory, IStrategoTerm[] prefix, int prefixEnd,
        IStrategoList tail) {

        IStrategoList result = tail;
        for(int i = prefixEnd - 1; i >= 0; i--) {
            result = factory.makeListCons(prefix[i], result);
        }
        return result;
    }

    private static IStrategoList atEndMaintainAnnos(Context context, IStrategoList list, Strategy s) {
        if(list.isEmpty()) {
            final IStrategoTerm tail = s.invoke(context, list);
            if(tail == null) {
                return null;
            }
            return checkListTail(tail);
        } else {
            final ITermFactory factory = context.getFactory();
            final IStrategoList tail = atEndMaintainAnnos(context, list.tail(), s);
            if(tail == null) {
                return null;
            }
            return (IStrategoList) factory.annotateTerm(factory.makeListCons(list.head(), tail), list.getAnnotations());
        }
    }
}
