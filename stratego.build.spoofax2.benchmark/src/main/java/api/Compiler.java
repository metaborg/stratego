package api;

import benchmark.exception.SkipException;
import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.*;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.store.SerializingStore;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.spoofax2.StrIncrModule;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.util.cmd.Arguments;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Compiler {
    protected static final String javaPackageName = "benchmark";

    private static final Path localRepository = Paths.get(System.getProperty("user.home"), ".m2", "repository");
    private static final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());

    private final Path baseDir;
    public final File javaDir;
    public final File classDir;
    private final Path pieDir;
    private final File packageDir;

    private final boolean library;
    private final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries;
    private final boolean autoImportStd;
    private final String metaborgVersion;
    private final LanguageIdentifier languageIdentifier;
    private final boolean output;
    private final Arguments args;

    private CompileOutput compiledProgram;
    private boolean javaCompilationResult = false;

    private Spoofax spoofax;
    private Pie pie;
    private final ModuleIdentifier mainModuleIdentifier;
    private final ArrayList<ResourcePath> strjIncludeDirs;
    private final ResourcePath projectPath;

    long timer;

    public Compiler(Path sourcePath, Arguments args, String metaborgVersion) throws IOException, MetaborgException {
        this(sourcePath, false, (ArrayList<IModuleImportService.ModuleIdentifier>) Collections.EMPTY_LIST, true, metaborgVersion, false, args);
    }

    public Compiler(Path sourcePath, boolean library, ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries, boolean autoImportStd, String metaborgVersion, boolean output, Arguments args) throws IOException, MetaborgException {
        this.library = library;
        this.linkedLibraries = linkedLibraries;
        this.autoImportStd = autoImportStd;
        this.metaborgVersion = metaborgVersion;
        this.output = output;
        this.args = args;

        String fileName = sourcePath.getFileName().toString();
        String baseName = FilenameUtils.removeExtension(fileName);

        Path tempDir = Files.createTempDirectory("stratego2benchmark");
        this.baseDir = tempDir.resolve(baseName);
        this.baseDir.toFile().deleteOnExit();

        this.javaDir = baseDir.resolve("java").toFile();
        this.classDir = baseDir.resolve("classes").toFile();
        this.pieDir = tempDir.resolve("pie");
        this.packageDir = javaDir.toPath().resolve(javaPackageName).toFile();

        languageIdentifier = new LanguageIdentifier("mb.stratego", "compnrun_" + baseName, new LanguageVersion(1));
        mainModuleIdentifier = new ModuleIdentifier(sourcePath.getFileName().toString().endsWith(".str"), this.library, baseName, new FSPath(sourcePath));
        projectPath = new FSPath(sourcePath.getParent());

        strjIncludeDirs = new ArrayList<>(1);
        strjIncludeDirs.add(projectPath);

        this.setupBuild();
    }

    public CompileOutput compileStratego() throws MetaborgException {
        System.out.println("Dropping PIE store... ");
        pie.dropStore();

        str2(args);
        assert compiledProgram instanceof CompileOutput.Success : "Compilation with stratego.lang compiler expected to succeed, but gave errors:\n" + getErrorMessagesString(compiledProgram);

        return compiledProgram;
    }

    public boolean compileJava() throws IOException, SkipException {
        if (!(compiledProgram instanceof CompileOutput.Success)) {
            throw new SkipException("Compilation with stratego.lang compiler expected to succeed, but gave errors:\n" + getErrorMessagesString(compiledProgram));
        }

        javaCompilationResult = Java.compile(classDir, javaFiles(), Collections.singletonList(getStrategoxtJarPath(metaborgVersion).toFile()), output);
        assert javaCompilationResult : "Compilation with javac expected to succeed";

        return javaCompilationResult;
    }

    public BufferedReader run() throws IOException, InterruptedException {
        assert javaCompilationResult : "Cannot run program: Java compilation did not succeed!";
        return Java.execute(classDir + ":" + Compiler.getStrategoxtJarPath(metaborgVersion), String.format("%s.Main", Compiler.javaPackageName));
    }

    private void setupBuild() throws MetaborgException {
        spoofax = new Spoofax(new StrIncrModule(), new GuiceTaskDefsModule());
        // compile

        final FSPath serializingStorePath =
                new FSPath(pieDir.resolve("pie-store"));

        System.out.print("Discovering Spoofax languages... ");
        timer = System.currentTimeMillis();
        // load Stratego language for later discovery during compilation (parsing in particular)
        spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(getStrategoPath(metaborgVersion).toFile()));
        spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(getStratego2Path(metaborgVersion).toFile()));

        System.out.printf(" (%d ms)%n", System.currentTimeMillis() - timer);

        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withLoggerFactory(StreamLoggerFactory.stdErrVeryVerbose());
        pieBuilder.withStoreFactory(
                (serde, resourceService, loggerFactory) -> new SerializingStore<>(serde,
                        resourceService.getWritableResource(serializingStorePath), InMemoryStore::new,
                        InMemoryStore.class));
        pieBuilder.withTaskDefs(spoofax.injector.getInstance(GuiceTaskDefs.class));
        System.out.print("Building PIE...");
        timer = System.currentTimeMillis();
        pie = pieBuilder.build();
        System.out.printf(" (%d ms)%n", System.currentTimeMillis() - timer);

        if (!linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib)) {
            linkedLibraries.add(BuiltinLibraryIdentifier.StrategoLib);
        }
    }

    private void str2(Arguments args) throws MetaborgException {
        System.out.print("Instantiating compile input...");
        timer = System.currentTimeMillis();
        CompileInput compileInput =
                new CompileInput(mainModuleIdentifier, projectPath, new FSPath(packageDir),
                        new FSPath(classDir), javaPackageName, new FSPath(pieDir.resolve("cacheDir")),
                        new ArrayList<>(0), strjIncludeDirs, linkedLibraries, args,
                        new ArrayList<>(0), library, autoImportStd, languageIdentifier.id, new ArrayList<>());
        System.out.printf(" (%d ms)%n", System.currentTimeMillis() - timer);

        System.out.print("Creating compile task...");
        timer = System.currentTimeMillis();
        Task<CompileOutput> compileTask =
                spoofax.injector.getInstance(Compile.class).createTask(compileInput);
        System.out.printf(" (%d ms)%n", System.currentTimeMillis() - timer);

        System.out.print("Generating new session...");
        timer = System.currentTimeMillis();
        try (MixedSession session = pie.newSession()) {
            System.out.printf(" (%d ms)%n", System.currentTimeMillis() - timer);
            System.out.println("Requiring task...");
            timer = System.currentTimeMillis();
            compiledProgram = Objects.requireNonNull(session.require(compileTask));
            System.out.printf("Task finished in %d ms%n", System.currentTimeMillis() - timer);

            int numOfJavaFiles = javaFiles().size();
            assert numOfJavaFiles > 0;

            System.out.println("Number of generated Java files: " + numOfJavaFiles);

            } catch (ExecException e) {
                throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(),
                        e);
            } catch (InterruptedException e) {
                throw new MetaborgException(
                        "Incremental Stratego build interrupted: " + e.getMessage(), e);
            }
    }

    public void cleanup() {
        try {
            System.out.println("Deleting intermediate results...");
            PathUtils.delete(baseDir);
        } catch (IOException e) {
            System.err.println("Some files could not be deleted:\n" + e);
        } catch (NullPointerException ignored) {}
    }

    private Collection<? extends File> javaFiles() {
        assert compiledProgram instanceof CompileOutput.Success : "Cannot get Java files from unsuccessful compilation!";

        final HashSet<ResourcePath> resultFiles = ((CompileOutput.Success) compiledProgram).resultFiles;
        final List<File> sourceFiles = new ArrayList<>(resultFiles.size());
        for (ResourcePath resultFile : resultFiles) {
            final @Nullable File localFile = resourceService.toLocalFile(resultFile);
            if (localFile == null) {
                throw new IllegalArgumentException("Result file '" + resultFile + "' cannot be converted to a local file");
            }
            sourceFiles.add(localFile);
        }
        return sourceFiles;
    }

    private static String getErrorMessagesString(CompileOutput str2CompileOutput) {
        return ((CompileOutput.Failure) str2CompileOutput).messages.stream()
                .filter(m -> m.severity == MessageSeverity.ERROR).map(Message::toString)
                .collect(Collectors.joining("\n"));
    }

    @NotNull
    private static Path getStratego2Path(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "stratego.lang", metaborgVersion, String.format("stratego.lang-%s.spoofax-language", metaborgVersion)));
    }

    @NotNull
    static Path getStrategoxtJarPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "strategoxt-jar", metaborgVersion, String.format("strategoxt-jar-%s.jar", metaborgVersion)));
    }

    @NotNull
    private static Path getStrategoPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "org.metaborg.meta.lang.stratego", metaborgVersion, String.format("org.metaborg.meta.lang.stratego-%s.spoofax-language", metaborgVersion)));
    }
}
