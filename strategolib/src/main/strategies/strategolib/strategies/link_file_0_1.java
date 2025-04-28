package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class link_file_0_1 extends Strategy {
    public static final link_file_0_1 instance = new link_file_0_1();

    /**
     * Stratego 2 type: {@code link-file :: (|string) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm existingPath, IStrategoTerm newPath) {
        return callStatic(context, existingPath, newPath);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm existingPath, IStrategoTerm newPath) {
        context.getIOAgent().printError("'SSL_link' is not implemented.");
        return null;
    }
}
