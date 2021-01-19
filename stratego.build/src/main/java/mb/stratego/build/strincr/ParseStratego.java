package mb.stratego.build.strincr;

import java.io.InputStream;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

public interface ParseStratego {
    IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path) throws Exception;

    IStrategoTerm parseRtree(InputStream inputStream) throws Exception;
}
