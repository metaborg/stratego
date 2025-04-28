package strategolib.strategies;

import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_table_hashtable_0_0 extends Strategy {
    public static final internal_table_hashtable_0_0 instance = new internal_table_hashtable_0_0();

    /**
     * Stratego 2 type: {@code internal-table-hashtable :: (|) ? -> HashtableImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOperatorRegistry registry = context.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
        if(registry != null) {
            final SSLLibrary sslLibrary = (SSLLibrary) registry;
            return sslLibrary.getTableTable();
        }
        return null;
    }
}
