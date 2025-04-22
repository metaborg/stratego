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
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;

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

public class StrcTests {
    public static final String packageName = "mb.stratego2integrationtest";
    public static final ResourceService resourceService =
        new DefaultResourceService(new FSResourceRegistry());

    @TestFactory Stream<DynamicTest> test1() throws URISyntaxException, IOException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        // tests 94 and 105 test something with externals, while not compiling against a library
        //   that contains that external.
        HashSet<String> disabledTestFiles =
            new HashSet<>(Arrays.asList("test113.str2", "test94.str2", "test105.str2"));
        final Predicate<Path> disableFilter =
            p -> !disabledTestFiles.contains(p.getFileName().toString());
        return compileAndRun("test1", "{test??.str2,test???.str2}", disableFilter,
            new ArrayList<>(Collections.singletonList(BuiltinLibraryIdentifier.StrategoSdf)));
    }

    @TestFactory
    Stream<DynamicTest> test2() throws URISyntaxException, IOException {
        // list-cons is not a test file, it is imported by other test files.
        HashSet<String> disabledTestFiles =
            new HashSet<>(Arrays.asList("list-cons.str2"));
        final Predicate<Path> disableFilter =
            p -> !disabledTestFiles.contains(p.getFileName().toString());
        return compileAndRun("test2", "*.str2", disableFilter, new ArrayList<>(0));
    }

    @TestFactory
    Stream<DynamicTest> testNeg() throws URISyntaxException, IOException {
        return failToCompile("testneg", "test*.str2", new ArrayList<>(0));
    }

    protected Stream<DynamicTest> compileAndRun(String subdir, String glob,
        Predicate<? super Path> disabled,
        ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws URISyntaxException, IOException {
        final Path strategoxtJarPath = Stratego.getStrategoxtJarPath();
        final Path strategoLibJarPath = Stratego.getStrategLibJarPath();
        final Path dirWithTestFiles = getResourcePathRoot().resolve(subdir);
        System.setProperty("user.dir", dirWithTestFiles.toAbsolutePath().toString());
        return streamStrategoFiles(dirWithTestFiles, glob).sorted().filter(disabled).map(p -> {
            final String fileName = p.getFileName().toString();
            // strip .str{2,}
            final String baseName = fileName.substring(0, fileName.lastIndexOf(".str"));
            final Path outputDir = p.resolveSibling(baseName + "/test-gen");
            final File outputDirFile = outputDir.toFile();
            return DynamicTest.dynamicTest("Compile & run " + baseName, () -> {
                FileUtils.deleteDirectory(outputDirFile);
                Files.createDirectories(outputDir);
                final LanguageIdentifier languageIdentifier =
                    new LanguageIdentifier("mb.stratego", "compnrun_" + baseName,
                        new LanguageVersion(1));
                final CompileOutput str2CompileOutput = Stratego
                    .str2(p, baseName, packageName, outputDir, false, linkedLibraries, false,
                        languageIdentifier);
                Assertions.assertTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors:\n"
                        + getErrorMessagesString(str2CompileOutput));
                final Iterable<? extends File> sourceFiles =
                    javaFiles((CompileOutput.Success) str2CompileOutput);
                Assertions.assertTrue(Java.compile(outputDir, sourceFiles,
                    Arrays.asList(outputDirFile, strategoLibJarPath.toFile(), strategoxtJarPath.toFile())),
                    "Compilation with javac expected to succeed (" + baseName + ")");
                Assertions.assertTrue(
                    Java.execute(Arrays.asList(outputDir, strategoLibJarPath, strategoxtJarPath), packageName + ".stratego2integrationtest"),
                    "Running java expected to succeed (" + baseName + ")");
            });
        });
    }

    @SuppressWarnings("SameParameterValue")
    protected Stream<DynamicTest> failToCompile(String subdir, String glob,
        ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws URISyntaxException, IOException {
        return failToCompile(subdir, glob, path -> true, linkedLibraries);
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
            // strip .str{2,}
            final String baseName = fileName.substring(0, fileName.lastIndexOf(".str"));
            final Path outputDir = p.resolveSibling(baseName + "/test-gen");
            return DynamicTest.dynamicTest("Compile & run " + baseName, () -> {
                FileUtils.deleteDirectory(outputDir.toFile());
                Files.createDirectories(outputDir);
                final LanguageIdentifier languageIdentifier =
                    new LanguageIdentifier("mb.stratego", "failtocomp_" + baseName,
                        new LanguageVersion(1));
                final CompileOutput compileOutput = Stratego
                    .str2(p, baseName, packageName, outputDir, true, linkedLibraries, false,
                        languageIdentifier);
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
            // strip .str{2,}
            final String baseName = fileName.substring(0, fileName.lastIndexOf(".str"));
            final Path outputDir = p.resolveSibling(baseName + "/test-gen");
            return DynamicTest.dynamicTest("Compile & run " + baseName, () -> {
                FileUtils.deleteDirectory(outputDir.toFile());
                Files.createDirectories(outputDir);
                final LanguageIdentifier languageIdentifier =
                    new LanguageIdentifier("mb.stratego", "comp_" + baseName,
                        new LanguageVersion(1));
                final CompileOutput str2CompileOutput = Stratego
                    .str2(p, baseName, packageName, outputDir, true, linkedLibraries, false,
                        languageIdentifier);
                Assertions.assertTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors:\n"
                        + getErrorMessagesString(str2CompileOutput));
            });
        });
    }

    static String getErrorMessagesString(CompileOutput str2CompileOutput) {
        final StringJoiner joiner = new StringJoiner("\n");
        for(Message m : ((CompileOutput.Failure) str2CompileOutput).messages) {
            if(m.severity == MessageSeverity.ERROR) {
                String toString = m.toString();
                joiner.add(toString);
            }
        }
        return joiner.toString();
    }

    private Stream<Path> streamStrategoFiles(Path dirWithTestFiles, String glob)
        throws IOException {
        final PathMatcher matcher =
            dirWithTestFiles.getFileSystem().getPathMatcher("glob:**/" + glob);
        return Files.list(dirWithTestFiles).filter(matcher::matches);
    }

    protected Path getResourcePathRoot() throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(this.getClass().getResource("/")).toURI());
    }

    protected static Iterable<? extends File> javaFiles(CompileOutput.Success str2CompileOutput) {
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

    @SuppressWarnings("unused")
    protected static Iterable<? extends File> javaFiles(Path packageDir) throws IOException {
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
