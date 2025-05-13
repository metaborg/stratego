package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoErrorExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.concat_strings_0_0;

public class fatal_err_0_3 extends Strategy {
    public static final fatal_err_0_3 instance = new fatal_err_0_3();

    /**
     * SRTS-EXT-fatal-err(|_, _, _)
     *
     * Stratego 2 type: {@code fatal-err :: (|?, ?, List(string)) a -> b}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm message,
        IStrategoTerm term, IStrategoTerm trace) {
        return callStatic(context, current, message, term, trace);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current, IStrategoTerm message,
        IStrategoTerm term, IStrategoTerm trace) {
        if(TermUtils.isList(message)) {
            message = concat_strings_0_0.instance.invoke(context, message);
        }
        if(message == null || !TermUtils.isString(message)) {
            return null;
        }

        context.popOnExit(false);
        if(TermUtils.isTuple(term) && term.getSubtermCount() == 0) {
            throw new StrategoErrorExit(TermUtils.toJavaString(message));
        } else {
            if(TermUtils.isList(trace)) {
                throw new StrategoErrorExit(TermUtils.toJavaString(message), term, (IStrategoList) trace);
            } else {
                throw new StrategoErrorExit(TermUtils.toJavaString(message), term);
            }
        }
    }
}
