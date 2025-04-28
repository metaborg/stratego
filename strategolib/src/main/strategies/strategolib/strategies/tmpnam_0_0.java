package strategolib.strategies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class tmpnam_0_0 extends Strategy {
    public static final tmpnam_0_0 instance = new tmpnam_0_0();

    /**
     * Stratego 2 type: {@code tmpnam :: (|) ? -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        Path tempFile;
        try {
            tempFile = Files.createTempFile("tempfiles", ".tmp");
        } catch(IOException e) {
            return null;
        }

        return context.getFactory().makeString(tempFile.toString());
    }
}
