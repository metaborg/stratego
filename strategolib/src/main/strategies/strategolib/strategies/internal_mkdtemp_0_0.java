package strategolib.strategies;

import java.io.File;
import java.io.IOException;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_mkdtemp_0_0 extends Strategy {
    public static internal_mkdtemp_0_0 instance = new internal_mkdtemp_0_0();

    /**
     * Stratego 2 type: {@code internal-mkdtemp :: (|) string -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        // HACK: We ignore the template directory, and just use it as a filename prefix
        String prefix = new File(TermUtils.toJavaString(current)).getName();
        if(prefix.endsWith("XXXXXX")) {
            prefix = prefix.substring(0, prefix.length() - 6);
        }

        final IOAgent agent = context.getIOAgent();
        final ITermFactory factory = context.getFactory();

        try {
            final String name = agent.createTempDir(prefix);
            return factory.makeString(name);
        } catch(IOException e) {
            return null;
        }
    }
}