package mb.stratego.build.spoofax2.integrationtest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.spoofax2.integrationtest.lang.Java;
import mb.stratego.build.spoofax2.integrationtest.lang.Stratego;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.metaborg.util.cmd.Arguments;

import javax.annotation.Nullable;

public class StrcTests {
    public static final String packageName = "mb.stratego2integrationtest";
    public static final String packageDirName = packageName.replace('.', '/');
    public static final ResourceService resourceService =
        new DefaultResourceService(new FSResourceRegistry());

    // TODO: turn shell scripts from test-strc into tests here

    @TestFactory Stream<DynamicTest> test1() throws URISyntaxException, IOException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        HashSet<String> disabledTestFiles =
            new HashSet<>(Collections.singletonList("test113.str2"));
        final Predicate<Path> disableFilter =
            p -> !disabledTestFiles.contains(p.getFileName().toString())
                    && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        return compileAndRun("test1", "test*.str2", disableFilter, new ArrayList<>(
            Arrays.asList(BuiltinLibraryIdentifier.StrategoLib,
                BuiltinLibraryIdentifier.StrategoSdf)));
    }

    @TestFactory
    Stream<DynamicTest> test2() throws URISyntaxException, IOException {
        // list-cons is not a test file, it is imported by other test files.
        HashSet<String> disabledTestFiles =
            new HashSet<>(Collections.singletonList("list-cons.str2"));
        final Predicate<Path> disableFilter =
            p -> !disabledTestFiles.contains(p.getFileName().toString())
                    && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        return compileAndRun("test2", "*.str2", disableFilter,
            new ArrayList<>(Arrays.asList(BuiltinLibraryIdentifier.StrategoLib)));
    }

    @TestFactory
    Stream<DynamicTest> testNeg() throws URISyntaxException, IOException {
        // test05 exposes problem where overlap check on dyn rule lhs is done too late in the process of the compiler
        HashSet<String> disabledTestFiles =
            new HashSet<>();//Arrays.asList("test05.str"));
        final Predicate<Path> disableFilter =
            p -> !disabledTestFiles.contains(p.getFileName().toString())
                    && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        return failToCompile("testneg", "test*.str2", disableFilter,
            new ArrayList<>(Arrays.asList(BuiltinLibraryIdentifier.StrategoLib)));
    }

    @TestFactory
    Stream<DynamicTest> testPMC() throws URISyntaxException, IOException {
        HashSet<String> disabledTestFiles =
                new HashSet<>(); //Arrays.asList("evalexpr.str2", "evalsym.str2", "evaltree.str2"));
        final Predicate<Path> disableFilter =
                p -> p.getFileName().toString().indexOf('.') == p.getFileName().toString().lastIndexOf('.')
                        && !disabledTestFiles.contains(p.getFileName().toString())
                        && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        return compileAndRun("test-pmc", "*.str2", disableFilter, new ArrayList<>(Arrays.asList(BuiltinLibraryIdentifier.StrategoLib)));
    }

    @TestFactory
    Stream<DynamicTest> testBenches() throws URISyntaxException, IOException {
        HashSet<String> disabledTestFiles =
                new HashSet<>(); //Arrays.asList("evalexpr.str2", "evalsym.str2", "evaltree.str2"));
        final Predicate<Path> disableFilter =
                p -> p.getFileName().toString().indexOf('.') == p.getFileName().toString().lastIndexOf('.')
                        && !disabledTestFiles.contains(p.getFileName().toString())
                        && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        return compileAndRun("test-benches", "*.str2", disableFilter, new ArrayList<>(Arrays.asList(BuiltinLibraryIdentifier.StrategoLib)));
    }

    protected Stream<DynamicTest> compileAndRun(String subdir, String glob,
                                                Predicate<? super Path> disabled,
                                                ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws URISyntaxException, IOException {
        final Arguments args = new Arguments();
        return compileAndRun(subdir, glob, disabled, linkedLibraries, args);
    }

    protected Stream<DynamicTest> compileAndRun(String subdir, String glob,
                                                Predicate<? super Path> disabled,
                                                ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries, Arguments args)
        throws URISyntaxException, IOException {
        final Path strategoxtJarPath = Stratego.getStrategoxtJarPath();
        final Path dirWithTestFiles = getResourcePathRoot().resolve(subdir);
        System.setProperty("user.dir", dirWithTestFiles.toAbsolutePath().toString());
        return streamStrategoFiles(dirWithTestFiles, glob).sorted().filter(disabled).map(p -> {
            final String fileName = p.getFileName().toString();
            final String baseName = FilenameUtils.removeExtension(fileName);
            final Path testGenDir = p.resolveSibling(baseName + "/test-gen");
            final Path packageDir = testGenDir.resolve(packageDirName);
            return DynamicTest.dynamicTest("Compile & run " + baseName, () -> {
                FileUtils.deleteDirectory(testGenDir.toFile());
                Files.createDirectories(packageDir);
                final CompileOutput str2CompileOutput = Stratego
                    .str2(p, baseName, packageName, packageDir, false, linkedLibraries, false, args);
                Assertions.assertTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors:\n"
                        + getErrorMessagesString(str2CompileOutput));
                final Iterable<? extends File> sourceFiles =
                    javaFiles((CompileOutput.Success) str2CompileOutput);
                Assertions.assertTrue(Java.compile(testGenDir, sourceFiles,
                    Collections.singletonList(strategoxtJarPath.toFile())),
                    "Compilation with javac expected to succeed");
                Assertions.assertTrue(
                    Java.execute(testGenDir + ":" + strategoxtJarPath, packageName + ".Main"),
                    "Running java expected to succeed");
            });
        });
    }

    @SuppressWarnings("SameParameterValue")
    protected Stream<DynamicTest> failToCompile(String subdir, String glob,
        Predicate<? super Path> disabled,
        ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws URISyntaxException, IOException {
        final Path dirWithTestFiles = getResourcePathRoot().resolve(subdir);
        System.setProperty("user.dir", dirWithTestFiles.toAbsolutePath().toString());
        return streamStrategoFiles(dirWithTestFiles, glob).filter(disabled).map(p -> {
            final String fileName = p.getFileName().toString();
            final String baseName = FilenameUtils.removeExtension(fileName); // strip .str
            final Path testGenDir = p.resolveSibling(baseName + "/test-gen");
            final Path packageDir = testGenDir.resolve(packageDirName);
            return DynamicTest.dynamicTest("Compile & run " + baseName, () -> {
                FileUtils.deleteDirectory(testGenDir.toFile());
                Files.createDirectories(packageDir);
                final CompileOutput compileOutput = Stratego
                    .str2(p, baseName, packageName, packageDir, true, linkedLibraries, false, new Arguments());
                Assertions.assertTrue(compileOutput instanceof CompileOutput.Failure,
                    "Compilation with stratego.lang compiler expected to fail");
            });
        });
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    protected Stream<DynamicTest> compiles(String subdir, String glob,
        ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws URISyntaxException, IOException {
        final Path dirWithTestFiles = getResourcePathRoot().resolve(subdir);
        System.setProperty("user.dir", dirWithTestFiles.toAbsolutePath().toString());
        return streamStrategoFiles(dirWithTestFiles, glob).map(p -> {
            final String fileName = p.getFileName().toString();
            final String baseName = fileName.substring(0, fileName.length() - 4); // strip .str
            final Path testGenDir = p.resolveSibling(baseName + "/test-gen");
            final Path packageDir = testGenDir.resolve(packageDirName);
            return DynamicTest.dynamicTest("Compile & run " + baseName, () -> {
                FileUtils.deleteDirectory(testGenDir.toFile());
                Files.createDirectories(packageDir);
                final CompileOutput str2CompileOutput = Stratego
                    .str2(p, baseName, packageName, packageDir, true, linkedLibraries, false, new Arguments());
                Assertions.assertTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors:\n"
                        + getErrorMessagesString(str2CompileOutput));
            });
        });
    }

    static String getErrorMessagesString(CompileOutput str2CompileOutput) {
        return ((CompileOutput.Failure) str2CompileOutput).messages.stream()
            .filter(m -> m.severity == MessageSeverity.ERROR).map(Message::toString)
            .collect(Collectors.joining("\n"));
    }

    private static Stream<Path> streamStrategoFiles(Path dirWithTestFiles, String glob)
        throws IOException {
        final PathMatcher matcher =
            dirWithTestFiles.getFileSystem().getPathMatcher("glob:**/" + glob);
        return Files.list(dirWithTestFiles).filter(matcher::matches);
    }

    protected Path getResourcePathRoot() throws URISyntaxException {
        return Paths.get(this.getClass().getResource("/").toURI());
    }

    static Iterable<? extends File> javaFiles(CompileOutput.Success str2CompileOutput) {
        final HashSet<ResourcePath> resultFiles = str2CompileOutput.resultFiles;
        final List<File> sourceFiles = new ArrayList<>(resultFiles.size());
        for(ResourcePath resultFile : resultFiles) {
            final @Nullable File localFile = resourceService.toLocalFile(resultFile);
            if(localFile == null) {
                throw new IllegalArgumentException("Result file '" + resultFile + "' cannot be converted to a local file");
            }
            sourceFiles.add(localFile);
        }
        return sourceFiles;
    }

    @SuppressWarnings("unused") protected static Iterable<? extends File> javaFiles(Path packageDir) throws IOException {
        final List<File> result = new ArrayList<>();
        try(DirectoryStream<Path> javaPaths = Files.newDirectoryStream(packageDir,
            p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".java"))) {
            for(Path javaPath : javaPaths) {
                result.add(javaPath.toFile());
            }
        }
        return result;
    }
}
