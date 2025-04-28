package strategolib.strategies;

import java.io.IOException;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_read_text_from_stream_0_0 extends Strategy {
    public static final internal_read_text_from_stream_0_0 instance = new internal_read_text_from_stream_0_0();

    /**
     * Stratego 2 type: {@code internal-read-text-from-stream :: (|) StreamImplBlob -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term) {
        return callStatic(context, term);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm term) {
        try {
            String resultString = context.getIOAgent().readString(TermUtils.toJavaInt(term));
            return context.getFactory().makeString(resultString);
        } catch (IOException e) {
            context.getIOAgent().printError("SSL_EXT_read_text_from_stream - could not read file (" + e.getMessage() + ")");
            return null;
        }
    }
}
