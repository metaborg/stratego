package api.til;

import benchmark.generic.Program;
import org.jetbrains.annotations.NotNull;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class TILProgram extends Program<TILLanguageProject> {

    public TILProgram(@NotNull Path sourcePath, int optimisationLevel, @NotNull String metaborgVersion) throws IOException, MetaborgException {

        File sourceFile = sourcePath.toFile();
        if (!(sourceFile.exists() && sourceFile.isFile())) {
            throw new FileNotFoundException(String.format("Input Stratego program not found: %s", sourcePath));
        }

        compiler = new TILLanguageProject(sourcePath, optimisationLevel, metaborgVersion);
    }

    public IStrategoTerm compileTIL() throws MetaborgException {
        return compiler.compileProgram();
    }

    public String run(Collection<String> inputString) throws MetaborgException {
        return compiler.run(inputString);
    }
}
