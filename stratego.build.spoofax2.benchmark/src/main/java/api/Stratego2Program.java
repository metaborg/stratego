package api;

import benchmark.exception.SkipException;
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
import mb.stratego.build.strincr.Stratego2LibInfo;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Stratego2Program {
    private static final Path localRepository = Paths.get(System.getProperty("user.home"), ".m2", "repository");

    private static final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
    private static final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries = new ArrayList<>();

    private static final String javaPackageName = "benchmark";

    private final Path baseDir;
    public final File javaDir;
    public final File classDir;
    private final Path pieDir;

    private final String baseName;
    private final Path sourcePath;
    private final String metaborgVersion;
    private final Arguments args = new Arguments();
    private final boolean output;

    private CompileOutput compiledProgram = null;
    private boolean javaCompilationResult = false;
    private LanguageIdentifier languageIdentifier;

    public Stratego2Program(Path sourcePath, String metaborgVersion, Arguments args) throws IOException {
        this(sourcePath, metaborgVersion, args, false);
    }

    public Stratego2Program(@NotNull Path sourcePath, @NotNull String metaborgVersion, Arguments args, boolean output) throws IOException {
        this.sourcePath = sourcePath;
        this.metaborgVersion = metaborgVersion;
        this.args.addAll(args);
        this.output = output;

        File sourceFile = sourcePath.toFile();
        if (!(sourceFile.exists() && sourceFile.isFile())) {
            throw new FileNotFoundException(String.format("Input Stratego program not found: %s", sourcePath));
        }

        String fileName = sourcePath.getFileName().toString();
        this.baseName = FilenameUtils.removeExtension(fileName);

        this.baseDir = Files.createTempDirectory("stratego2benchmark").resolve(baseName);
        this.baseDir.toFile().deleteOnExit();

        this.javaDir = baseDir.resolve("java").toFile();
        this.classDir = baseDir.resolve("classes").toFile();
        this.pieDir = baseDir.resolve("pie");
        languageIdentifier =
                new LanguageIdentifier("mb.stratego", "compnrun_" + baseName,
                        new LanguageVersion(1));
    }

    private Iterable<? extends File> javaFiles() {
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

    // Take inspiration from stratego.build.spoofax2.integrationtest

    private static String getErrorMessagesString(CompileOutput str2CompileOutput) {
        return ((CompileOutput.Failure) str2CompileOutput).messages.stream()
                .filter(m -> m.severity == MessageSeverity.ERROR).map(Message::toString)
                .collect(Collectors.joining("\n"));
    }

    @NotNull
    private static Path getStrategoxtJarPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "strategoxt-jar", metaborgVersion, String.format("strategoxt-jar-%s.jar", metaborgVersion)));
    }

    @NotNull
    private static Path getStrategoPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "org.metaborg.meta.lang.stratego", metaborgVersion, String.format("org.metaborg.meta.lang.stratego-%s.spoofax-language", metaborgVersion)));
    }

    @NotNull
    private static Path getStratego2Path(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "stratego.lang", metaborgVersion, String.format("stratego.lang-%s.spoofax-language", metaborgVersion)));
    }

    @SuppressWarnings("SameParameterValue")
    private CompileOutput str2(Path input, String baseName, String packageName,
                               File packageDir, boolean library,
                               ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries, boolean autoImportStd, Arguments args, String metaborgVersion, LanguageIdentifier languageIdentifier)
            throws MetaborgException {

        try (Spoofax spoofax = new Spoofax(new StrIncrModule(), new GuiceTaskDefsModule())) {
            // compile

            final FSPath serializingStorePath =
                    new FSPath(pieDir.resolve("pie-store"));

            // load Stratego language for later discovery during compilation (parsing in particular)
            spoofax.languageDiscoveryService
                    .languageFromArchive(spoofax.resolve(getStrategoPath(metaborgVersion).toFile()));
            spoofax.languageDiscoveryService
                    .languageFromArchive(spoofax.resolve(getStratego2Path(metaborgVersion).toFile()));

            final PieBuilder pieBuilder = new PieBuilderImpl();
            pieBuilder.withStoreFactory(
                    (serde, resourceService, loggerFactory) -> new SerializingStore<>(serde,
                            resourceService.getWritableResource(serializingStorePath), InMemoryStore::new,
                            InMemoryStore.class));
            pieBuilder.withTaskDefs(spoofax.injector.getInstance(GuiceTaskDefs.class));
            Pie pie = pieBuilder.build();

            final ResourcePath projectPath = new FSPath(input.getParent());

            if (!linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib)) {
                linkedLibraries.add(BuiltinLibraryIdentifier.StrategoLib);
            }
            final ArrayList<ResourcePath> strjIncludeDirs = new ArrayList<>(1);
            strjIncludeDirs.add(projectPath);

            final ModuleIdentifier mainModuleIdentifier =
                    new ModuleIdentifier(input.getFileName().toString().endsWith(".str"), library, baseName, new FSPath(input));
            final Stratego2LibInfo stratego2LibInfo =
                    new Stratego2LibInfo(packageName, languageIdentifier.groupId, languageIdentifier.id,
                            languageIdentifier.version.toString(),
                            new ArrayList<>(Collections.singletonList(new FSPath("stratego.jar"))));

            CompileInput compileInput =
                    new CompileInput(mainModuleIdentifier, projectPath, new FSPath(packageDir),
                            new FSPath(classDir), packageName, new FSPath(pieDir.resolve("cacheDir")),
                            new ArrayList<>(0), strjIncludeDirs, linkedLibraries, args,
                            new ArrayList<>(0), library, autoImportStd, languageIdentifier.id, stratego2LibInfo);
            Task<CompileOutput> compileTask =
                    spoofax.injector.getInstance(Compile.class).createTask(compileInput);

            try (final MixedSession session = pie.newSession()) {
                return Objects.requireNonNull(session.require(compileTask));
            } catch (ExecException e) {
                throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(),
                        e);
            } catch (InterruptedException e) {
                throw new MetaborgException(
                        "Incremental Stratego build interrupted: " + e.getMessage(), e);
            }
        }
    }

    public void cleanup() {
        try {
            PathUtils.delete(baseDir);
        } catch (IOException e) {
            System.err.println("Some files could not be deleted:\n" + e);
        } catch (NullPointerException ignored) {}
    }

    public CompileOutput compileStratego() throws MetaborgException {
        compiledProgram = str2(sourcePath, baseName, javaPackageName, javaDir.toPath().resolve(javaPackageName).toFile(), false, linkedLibraries, true, args, metaborgVersion, languageIdentifier);
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
        return Java.execute(classDir + ":" + getStrategoxtJarPath(metaborgVersion), String.format("%s.Main", javaPackageName));
    }

}
