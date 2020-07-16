package mb.stratego.build.strincr;

import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface ParseStratego {
    IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path) throws Exception;

    IStrategoTerm parseRtree(InputStream inputStream) throws Exception;
}
