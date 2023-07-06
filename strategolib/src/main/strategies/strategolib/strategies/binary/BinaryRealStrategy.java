package strategolib.strategies.binary;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public abstract class BinaryRealStrategy extends Strategy {
    /**
     * Stratego 2 type: {@code (|real) real -> real}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm left, IStrategoTerm right) {
        return context.getFactory().makeReal(operation(TermUtils.toJavaReal(left), TermUtils.toJavaReal(right)));
    }

    public abstract double operation(double left, double right);
}
