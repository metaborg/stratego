package strategolib.strategies;

import java.io.IOException;
import java.io.Writer;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;

public class internal_write_term_to_stream_text_0_1 extends Strategy {
    public static final internal_write_term_to_stream_text_0_1 instance = new internal_write_term_to_stream_text_0_1();

    /**
     * Stratego 2 type: {@code internal-write-term-to-stream-text :: (|StreamImplBlob) ? -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, IStrategoTerm streamInt) {
        return callStatic(context, term, streamInt);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm term, IStrategoTerm streamInt) {
        final int streamNo = TermUtils.toJavaInt(streamInt);

        final Writer out = context.getIOAgent().getWriter(streamNo);
        if(out == null) {
            return null;
        }

        try {
            term.writeAsString(out, Integer.MAX_VALUE);
            out.flush();
        } catch(IOException e) {
            throw new StrategoException("Exception in execution of primitive 'SSL_write_term_to_stream_text'", e);
        }

        return streamInt;
    }
}
