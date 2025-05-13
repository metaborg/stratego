package api;

import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import org.apache.commons.io.FilenameUtils;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public abstract class Compiler<R> {
    protected static final Path localRepository = Paths.get(System.getProperty("user.home"), ".m2", "repository");
    protected static final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());

    protected Spoofax spoofax;
    protected final String metaborgVersion;

    protected final Path baseDir;
    protected final File compileDir;
    protected final Path tempDir;

    protected final String baseName;
    protected final Path sourcePath;
    protected R compiledProgram;

    protected Compiler(String metaborgVersion, Path sourcePath) throws IOException {
        this.metaborgVersion = metaborgVersion;

        this.sourcePath = sourcePath.toAbsolutePath();

        String fileName = this.sourcePath.getFileName().toString();
        baseName = FilenameUtils.removeExtension(fileName);

        tempDir = Files.createTempDirectory("stratego2benchmark");

        baseDir = tempDir.resolve(baseName);
        baseDir.toFile().deleteOnExit();

        compileDir = baseDir.resolve("compile").toFile();
    }

    public abstract R compileProgram() throws MetaborgException, IOException;

//    public abstract CharSequence run() throws IOException, InterruptedException, MetaborgException;

    protected abstract void setupBuild() throws MetaborgException;

    public abstract void cleanup();
}
