package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

public class internal_rename_file_0_1 extends Strategy {
    public static final internal_rename_file_0_1 instance = new internal_rename_file_0_1();

    /**
     * Stratego 2 type: {@code rename-file :: (|C99FileLoc) * C99FileLoc -> C99FileLoc}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm oldName, IStrategoTerm newName) {
        return callStatic(context, oldName, newName);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm oldName, IStrategoTerm newName) {
        throw new StrategoException("Primitive not defined: SSL_rename_file");
    }
}
