package api.stratego2;

import benchmark.exception.SkipException;
import benchmark.generic.Program;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.jetbrains.annotations.NotNull;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Stratego2Program extends Program<Stratego2Compiler> {

    private static final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries = new ArrayList<>();

    public Stratego2Program(Path sourcePath, Arguments args, String metaborgVersion) throws IOException, MetaborgException {
        this(sourcePath, args, false, true, metaborgVersion, true);
    }

    public Stratego2Program(@NotNull Path sourcePath, Arguments args, boolean library, boolean autoImportStd, @NotNull String metaborgVersion, boolean output) throws IOException, MetaborgException {

        File sourceFile = sourcePath.toFile();
        if (!(sourceFile.exists() && sourceFile.isFile())) {
            throw new FileNotFoundException(String.format("Input Stratego program not found: %s", sourcePath));
        }

        compiler = new Stratego2Compiler(
                sourcePath,
                library,
                linkedLibraries,
                autoImportStd,
                metaborgVersion,
                output,
                args);
    }

    public CompileOutput compileStratego() throws MetaborgException, IOException {
        return compiler.compileProgram();
    }

    /**
     * @return
     * @throws IOException
     * @throws SkipException
     */
    public File compileJava() throws IOException {
        return compiler.compileJava();
    }

    public String run(String input) throws IOException, InterruptedException {
        return compiler.run(input);
    }

    public boolean compileStrj() throws IOException {
        return compiler.strj();
    }
}
