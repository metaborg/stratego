package api.stratego2;

import api.Compiler;
import api.Java;
import api.SimpleSpoofaxModule;
import benchmark.exception.SkipException;
import com.google.common.collect.Lists;

import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.*;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.SerializingStoreBuilder;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.resource.fs.FSPath;
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
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.vfs2.FileObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingFacet;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.strj.strj;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class Stratego2Compiler extends Compiler<CompileOutput> {
    private static final String javaPackageName = "benchmark";
    private final File classDir;

    private final Path pieDir;

    private final boolean library;
    private final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries;
    private final boolean autoImportStd;
    private final LanguageIdentifier languageIdentifier;
    private final boolean output;
    private final Arguments args;
    private final File packageDir;

    private boolean javaCompilationResult;

    private Pie pie;
    private Session session;
    private final ModuleIdentifier mainModuleIdentifier;
    private final LinkedHashSet<ResourcePath> strjIncludeDirs;
    private final ResourcePath projectPath;

    private long timer;

    public Stratego2Compiler(Path sourcePath, Arguments args, String metaborgVersion) throws IOException, MetaborgException {
        this(sourcePath, false, new ArrayList<>(), true, metaborgVersion, false, args);
    }

    public Stratego2Compiler(Path sourcePath, boolean library, ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries, boolean autoImportStd, String metaborgVersion, boolean output, Arguments args) throws IOException, MetaborgException {
        super(metaborgVersion, sourcePath);

        this.library = library;
        this.linkedLibraries = linkedLibraries;
        this.autoImportStd = autoImportStd;
        this.output = output;
        this.args = args;

        pieDir = tempDir.resolve("pie");
        packageDir = compileDir.toPath().resolve(javaPackageName).toFile();
        classDir = baseDir.resolve("classes").toFile();

        languageIdentifier = new LanguageIdentifier("mb.stratego", "compnrun_" + baseName, new LanguageVersion(1));
        mainModuleIdentifier = new ModuleIdentifier(sourcePath.getFileName().toString().endsWith(".str"), this.library, baseName, new FSPath(sourcePath));
        projectPath = new FSPath(sourcePath.getParent());

        strjIncludeDirs = new LinkedHashSet<>(1);
        strjIncludeDirs.add(projectPath);

        setupBuild();
    }

    @Override
    public CompileOutput compileProgram() throws MetaborgException, IOException {
        if (null != session) {
            debugPrintln("Dropping PIE store... ");
            session.dropStore();
        }

        str2(args);
        if (!(compiledProgram instanceof CompileOutput.Success))
            throw new RuntimeException("Compilation with stratego.lang compiler expected to succeed, but gave errors:\n" + getErrorMessagesString(compiledProgram));

        System.out.println("Size of generated Java files (bytes): " + javaFiles().stream().map(File::toPath).mapToLong(path -> {
            try {
                return Files.size(path);
            } catch (IOException e) {
                e.printStackTrace();
                return Long.MIN_VALUE;
            }
        }).sum());

        return compiledProgram;
    }

    /**
     * @return
     * @throws IOException
     * @throws SkipException
     */
    public File compileJava() throws IOException {
        if (!(compiledProgram instanceof CompileOutput.Success)) {
            throw new SkipException("Compilation with stratego.lang compiler expected to succeed, but gave errors:\n" + getErrorMessagesString(compiledProgram));
        }

        debugPrintf("Compiling %d Java files...%n", javaFiles().size());
        timer = System.currentTimeMillis();
        javaCompilationResult = Java.compile(classDir, javaFiles(), Arrays.asList(getStrategoLibJarPath(metaborgVersion).toFile(), getStrategoxtJarPath(metaborgVersion).toFile()), output);
        debugPrintf("Done! (%d ms)%n", System.currentTimeMillis() - timer);

        if (!javaCompilationResult)
            throw new RuntimeException("Compilation with javac expected to succeed");

        System.out.println("Size of generated class files (bytes): " + Files.walk(classDir.toPath()).mapToLong(path -> {
            try {
                return Files.size(path);
            } catch (IOException e) {
                e.printStackTrace();
                return Long.MIN_VALUE;
            }
        }).sum());

        return classDir;
    }

//    @Override
    public String run(String input) throws IOException, InterruptedException {
        if (!javaCompilationResult)
            throw new RuntimeException("Cannot run program: Java compilation did not succeed!");

        return Java.execute(classDir + ":" + getStrategoLibJarPath(metaborgVersion) + ":" + getStrategoxtJarPath(metaborgVersion), String.format("%s.Main", javaPackageName), input).lines().collect(Collectors.joining());
    }

    @Override
    protected void setupBuild() throws MetaborgException {
        spoofax = new Spoofax(new SimpleSpoofaxModule(), new StrIncrModule(),
            new GuiceTaskDefsModule());
        // compile

        FSPath serializingStorePath =
                new FSPath(pieDir.resolve("pie-store"));

        debugPrint("Discovering Spoofax languages... ");
        timer = System.currentTimeMillis();
        // load Stratego language for later discovery during compilation (parsing in particular)
//        spoofax.languageDiscoveryService
//                .languageFromArchive(spoofax.resolve(getStrategoPath(metaborgVersion).toFile()));
        // (stratego 2)
        spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(getStratego2Path(metaborgVersion).toFile()));
        // (strategolib)
        spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(getStrategoLibPath(metaborgVersion).toFile()));

        debugPrintf(" (%d ms)%n", System.currentTimeMillis() - timer);

        PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withLoggerFactory(StreamLoggerFactory.stdErrVeryVerbose());
        pieBuilder.withStoreFactory(
                (serde, resourceService, loggerFactory) ->
                        SerializingStoreBuilder
                                .ofInMemoryStore(serde)
                                .withResourceStorage(resourceService.getWritableResource(serializingStorePath))
                                .build());
        pieBuilder.withTaskDefs(spoofax.injector.getInstance(GuiceTaskDefs.class));
        debugPrint("Building PIE...");
        timer = System.currentTimeMillis();
        pie = pieBuilder.build();
        debugPrintf(" (%d ms)%n", System.currentTimeMillis() - timer);

//        if (!linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib)) {
//            linkedLibraries.add(BuiltinLibraryIdentifier.StrategoLib);
//        }
    }

    private void str2(Arguments args) throws MetaborgException, IOException {
        debugPrint("Instantiating compile input...");
        timer = System.currentTimeMillis();
        final LinkedHashSet<Supplier<Stratego2LibInfo>> str2libraries = new LinkedHashSet<>();
        if(!linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib)) {
            final Path temporaryDirectoryPath =
                    Files.createTempDirectory("mb.stratego.build.spoofax2.benchmark")
                            .toAbsolutePath();
            // load strategolib language (str2lib)
            final ILanguageImpl sourceDepImpl = spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(getStrategoLibPath(metaborgVersion).toFile()));
            for(ILanguageComponent sourceDepImplComp : sourceDepImpl.components()) {
                final String[] str2libProject = { null };
                for(IExportConfig export : sourceDepImplComp.config().exports()) {
                    if(str2libProject[0] != null) {
                        break;
                    }
                    export.accept(new IExportVisitor() {
                        @Override public void visit(LangDirExport resource) {}

                        @Override public void visit(LangFileExport resource) {
                            if(resource.language.equals("StrategoLang") && resource.file.endsWith("str2lib")) {
                                str2libProject[0] = resource.file;
                            }
                        }

                        @Override public void visit(ResourceExport resource) {}
                    });
                }
                if(str2libProject[0] != null) {
                    final FileObject strjIncludes =
                        spoofax.resourceService.resolve(temporaryDirectoryPath.resolve("strj-includes").toFile());
                    final FileObject str2LibFileObject = sourceDepImplComp.location().resolveFile(str2libProject[0]);
                    final ResourcePath str2LibFile =
                        new FSPath(spoofax.resourceService.localFile(str2LibFileObject, strjIncludes));
                    final @Nullable DynamicClassLoadingFacet facet =
                        sourceDepImplComp.facet(DynamicClassLoadingFacet.class);
                    if(facet == null) {
                        continue;
                    }
                    final ArrayList<ResourcePath> jarFiles =
                        new ArrayList<>(facet.jarFiles.size());
                    for(FileObject file : facet.jarFiles) {
                        jarFiles.add(new FSPath(spoofax.resourceService.localFile(file, strjIncludes)));
                    }
                    str2libraries.add(new ValueSupplier<>(new Stratego2LibInfo(str2LibFile, jarFiles)));
                }
            }
        }
        CompileInput compileInput =
                new CompileInput(mainModuleIdentifier, projectPath, new FSPath(packageDir),
                        new FSPath(classDir), javaPackageName, new FSPath(pieDir.resolve("cacheDir")),
                        new ArrayList<>(0), strjIncludeDirs, linkedLibraries, args,
                        new ArrayList<>(0), library, autoImportStd, true, languageIdentifier.id, str2libraries, true, true, null);
        debugPrintf(" (%d ms)%n", System.currentTimeMillis() - timer);

        debugPrint("Creating compile task...");
        timer = System.currentTimeMillis();
        Task<CompileOutput> compileTask =
                spoofax.injector.getInstance(Compile.class).createTask(compileInput);
        debugPrintf(" (%d ms)%n", System.currentTimeMillis() - timer);

        debugPrint("Generating new session...");
        timer = System.currentTimeMillis();
        session = pie.newSession();
        debugPrintf(" (%d ms)%n", System.currentTimeMillis() - timer);
        debugPrintln("Requiring task...");
        timer = System.currentTimeMillis();

        try {
            compiledProgram = Objects.requireNonNull(session.require(compileTask));
            debugPrintf("Task finished in %d ms%n", System.currentTimeMillis() - timer);
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
            debugPrintln("Deleting intermediate results...");
            PathUtils.cleanDirectory(baseDir);
        } catch (NoSuchFileException e) {
            System.err.println("File already deleted: " + e.getFile());
        } catch (IOException e) {
            System.err.println("Some files could not be deleted:\n" + e);
        }
    }

    private Collection<File> javaFiles() {
        return javaFiles(compiledProgram);
    }

    public static Collection<File> javaFiles(CompileOutput compiledProgram) {
        if (!(compiledProgram instanceof CompileOutput.Success))
            throw new RuntimeException("Cannot get Java files from unsuccessful compilation!");

        Set<ResourcePath> resultFiles = ((CompileOutput.Success) compiledProgram).resultFiles;
        Collection<File> sourceFiles = new ArrayList<>(resultFiles.size());
        for (ResourcePath resultFile : resultFiles) {
            @Nullable File localFile = resourceService.toLocalFile(resultFile);
            if (null == localFile) {
                throw new IllegalArgumentException("Result file '" + resultFile + "' cannot be converted to a local file");
            }
            sourceFiles.add(localFile);
        }
        return sourceFiles;
    }

    private static String getErrorMessagesString(CompileOutput str2CompileOutput) {
        return ((CompileOutput.Failure) str2CompileOutput).messages.stream()
                .filter(m -> MessageSeverity.ERROR == m.severity).map(Message::toString)
                .collect(Collectors.joining("\n"));
    }

    public boolean strj() throws IOException {
        strj.init();

        System.out.println("Creating packagedir...");
        Files.createDirectories(packageDir.toPath());

//        System.out.println("Creating javadir...");
//        Files.createDirectories()

//        System.out.println("Args: " + args.toString());

        List<String> strjArgs = Lists.newArrayList(
                "-i", sourcePath.toString(),
                "-o", packageDir.toPath().resolve("Main.java").toString(),
                "-p", javaPackageName,
                "-la", "stratego-lib",
                "-D", "VERSION_TERM=\"${version}\"",
                "-D", "SVN_REVISION_TERM=\"${revision}\"",
//                "-I", "../../src/main/strategies",
//                "-I", "../../src/main/strategies/ssl-compat",
                "-m", "main-" + baseName,
                "--verbose", "error"
        );

        strjArgs.addAll(args.asStrings(null));

        IStrategoTerm result;
        try {
            //@formatter:off
            result = strj.mainNoExit(strjArgs.toArray(new String[strjArgs.size()]));
            //@formatter:on
        } catch(StrategoExit exit) {
            return 0 == exit.getValue();
        }
        return null != result;
    }

    @NotNull
    private static Path getStratego2Path(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "stratego.lang", metaborgVersion, String.format("stratego.lang-%s.spoofax-language", metaborgVersion)));
    }

    @NotNull
    private static Path getStrategoLibJarPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "strategolib", metaborgVersion, String.format("strategolib-%s.jar", metaborgVersion)));
    }

    private static Path getStrategoLibPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "strategolib", metaborgVersion, String.format("strategolib-%s.spoofax-language", metaborgVersion)));
    }

    @NotNull
    private static Path getStrategoxtJarPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "strategoxt-jar", metaborgVersion, String.format("strategoxt-jar-%s.jar", metaborgVersion)));
    }

    @NotNull
    private static Path getStrategoPath(String metaborgVersion) {
        return localRepository.resolve(Paths.get("org", "metaborg", "org.metaborg.meta.lang.stratego", metaborgVersion, String.format("org.metaborg.meta.lang.stratego-%s.spoofax-language", metaborgVersion)));
    }

    private void debugPrintln(Object x) {
        if (output) System.out.println(x);
    }

    private void debugPrint(Object x) {
        if (output) System.out.print(x);
    }

    private void debugPrintf(String format, Object... args) {
        if (output) System.out.printf(format, args);
    }
}

