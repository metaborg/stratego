package api;

import benchmark.exception.SkipException;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.apache.commons.io.FilenameUtils;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public abstract class Compiler {
    protected static final Path localRepository = Paths.get(System.getProperty("user.home"), ".m2", "repository");

    protected Spoofax spoofax;
    protected final String metaborgVersion;

    protected final Path baseDir;
    public final File javaDir;
    public final File classDir;
    protected File packageDir;
    protected final Path tempDir;

    protected final String baseName;
    private final String fileName;
    protected final Path sourcePath;

    public Compiler(String metaborgVersion, Path sourcePath) throws IOException {
        this.metaborgVersion = metaborgVersion;

        this.sourcePath = sourcePath.toAbsolutePath();

        fileName = this.sourcePath.getFileName().toString();
        baseName = FilenameUtils.removeExtension(fileName);

        this.tempDir = Files.createTempDirectory("stratego2benchmark");

        this.baseDir = tempDir.resolve(baseName);
        this.baseDir.toFile().deleteOnExit();

        this.javaDir = baseDir.resolve("java").toFile();
        this.classDir = baseDir.resolve("classes").toFile();
    }

    public abstract CompileOutput compileStratego() throws MetaborgException;

    public abstract File compileJava() throws IOException, SkipException;

    public abstract BufferedReader run() throws IOException, InterruptedException;

    protected abstract Collection<File> javaFiles();
}
