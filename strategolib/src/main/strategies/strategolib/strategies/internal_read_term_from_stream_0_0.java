package strategolib.strategies;

import java.io.IOException;
import java.io.InputStream;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_read_term_from_stream_0_0 extends Strategy {
    public static final internal_read_term_from_stream_0_0 instance = new internal_read_term_from_stream_0_0();

    /**
     * Stratego 2 type: {@code internal-write-term-to-stream-text :: (|StreamImplBlob) ? -> StreamImplBlob}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm term) {
        return callStatic(context, term);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm term) {
        final int streamNo = TermUtils.toJavaInt(term);

        // TODO: optimize - use memory-mapped I/O for reading terms?
        // PushBackInputStream.read() is very inefficient;
        // why not create our own implementation based on memory-mapped I/O?
        //
        // UNDONE: tricky: detecting if it's a BAF term in TermFactory...
        //
        // FileChannel channel = or.getIOAgent().getInputChannel(TermUtils.toJavaInt(tvars[0]));
        // ChannelPushbackInputStream reader = new ChannelPushbackInputStream(channel);
        // TODO: in other places we're using getReader(); seems risky to use internalGetInputStream() here
        final IOAgent ioAgent = context.getIOAgent();
        final InputStream is = ioAgent.internalGetInputStream(streamNo);
        if(is == null)
            return null;

        try {
            return new TermReader(context.getFactory()).parseFromStream(is);
        } catch(IOException e) {
            ioAgent.printError("SSL_read_term_from_stream: " + e.getMessage());
            return null;
        } catch(ParseError e) {
            ioAgent.printError("SSL_read_term_from_stream: " + e.getMessage());
            return null;
        }
    }
}
