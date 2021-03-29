package mb.stratego.build.strincr;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.charset.Charset;

import mb.pie.api.ExecException;
import mb.stratego.build.strincr.data.GTEnvironment;

public interface StrategoLanguage {
    IStrategoTerm parse(InputStream inputStream, Charset charset, @Nullable String path) throws Exception;

    IStrategoTerm parseRtree(InputStream inputStream) throws Exception;

    IStrategoTerm insertCasts(String moduleName, GTEnvironment environment, String projectPath) throws
        ExecException;

    IStrategoTerm desugar(IStrategoTerm ast, String projectPath) throws ExecException;

    IStrategoTerm toJava(IStrategoList buildInput, String projectPath) throws ExecException;
}
