package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class write_to_shared_string_0_0 extends Strategy {
    public static final write_to_shared_string_0_0 instance = new write_to_shared_string_0_0();

    /**
     * Stratego 2 type: {@code write-to-shared-string :: (|) a -> List(Char)}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term) {
        return callStatic(context, term);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm term) {
        context.getIOAgent().printError("'SSL_write_term_to_shared_string' is not implemented");
        return null;
    }
}
