package api;

import benchmark.exception.SkipException;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.jetbrains.annotations.NotNull;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Stratego2Program {

    private static final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries = new ArrayList<>();

    public final Compiler compiler;

    public Stratego2Program(Path sourcePath, Arguments args, String metaborgVersion) throws IOException, MetaborgException {
        this(sourcePath, args, false, true, metaborgVersion, false);
    }

    public Stratego2Program(@NotNull Path sourcePath, Arguments args, boolean library, boolean autoImportStd, @NotNull String metaborgVersion, boolean output) throws IOException, MetaborgException {

        File sourceFile = sourcePath.toFile();
        if (!(sourceFile.exists() && sourceFile.isFile())) {
            throw new FileNotFoundException(String.format("Input Stratego program not found: %s", sourcePath));
        }

        this.compiler = new Compiler(
                sourcePath,
                library,
                linkedLibraries,
                autoImportStd,
                metaborgVersion,
                output,
                args);
    }

    public void cleanup() {
        compiler.cleanup();
    }

    public CompileOutput compileStratego() throws MetaborgException {
        return compiler.compileStratego();
    }

    public boolean compileJava() throws IOException, SkipException {
        return compiler.compileJava();
    }

    public BufferedReader run() throws IOException, InterruptedException {
        return compiler.run();
    }

}
