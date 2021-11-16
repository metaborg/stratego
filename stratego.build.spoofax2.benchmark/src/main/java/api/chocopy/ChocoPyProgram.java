package api.chocopy;

import benchmark.generic.Program;
import org.jetbrains.annotations.NotNull;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class ChocoPyProgram extends Program<ChocoPyCompiler> {

    public ChocoPyProgram(@NotNull Path sourcePath, int optimisationLevel, @NotNull String metaborgVersion) throws IOException, MetaborgException {

        File sourceFile = sourcePath.toFile();
        if (!(sourceFile.exists() && sourceFile.isFile())) {
            throw new FileNotFoundException(String.format("Input Stratego program not found: %s", sourcePath));
        }

        compiler = new ChocoPyCompiler(sourcePath, optimisationLevel, metaborgVersion);
    }

    public Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> compileChocoPy() throws MetaborgException {
        return compiler.compileProgram();
    }

    public String run() throws MetaborgException {
        return compiler.prettyPrint();
    }
}
