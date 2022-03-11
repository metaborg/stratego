package strategolib.strategies;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class flatten_list_0_0 extends Strategy {
    public static flatten_list_0_0 instance = new flatten_list_0_0();

    /**
     * Stratego 2 type: {@code flatten-list :: (|) List(?) -> List(?)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        ArrayList<IStrategoTerm> newList = new ArrayList<IStrategoTerm>();
        ArrayList<IStrategoTerm> stack = new ArrayList<IStrategoTerm>();

        stack.add(current);

        while (!stack.isEmpty()) {
            current = stack.remove(stack.size() - 1);

            if (TermUtils.isList(current)) {
                IStrategoList list = TermUtils.toList(current);
                final int oldsize = stack.size();
                while (!list.isEmpty()) {
                    stack.add(list.head());
                    list = list.tail();
                }
                reverse(stack, oldsize, stack.size());
            }
            else {
                newList.add(current);
            }
        }

        current = context.getFactory().makeList(newList);
        return current;
    }

    private void reverse(ArrayList<IStrategoTerm> array, int start, int end) {
        final int length = (end - start) / 2;
        for (int i = 0; i < length; ++i) {
            swap(array, start + i, end - 1 - i);
        }
    }

    private void swap(ArrayList<IStrategoTerm> array, int i, int j) {
        final IStrategoTerm tmp = array.get(i);
        array.set(i, array.get(j));
        array.set(j, tmp);
    }
}
