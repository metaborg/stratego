package strategolib.strategies.binary;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public abstract class BinaryIntegerStrategy extends Strategy {
    /**
     * Stratego 2 type: {@code (|int) int -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm left, IStrategoTerm right) {
        return context.getFactory().makeInt(operation(TermUtils.toJavaInt(left), TermUtils.toJavaInt(right)));
    }

    public abstract int operation(int left, int right);
}
