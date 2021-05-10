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
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
import mb.stratego.build.strincr.task.output.CompileOutput;

public class StrcTests {
    public static final String packageName = "mb.stratego2integrationtest";
    public static final String packageDirName = packageName.replace('.', '/');
    public static final ResourceService resourceService =
        new DefaultResourceService(new FSResourceRegistry());

    // TODO: turn shell scripts from test-strc into tests here

    @Disabled("To be enabled when the bugs found by these tests are removed")
    @TestFactory Stream<DynamicTest> test1() throws URISyntaxException, IOException {
        return compileAndRun("test1", "{test??.str,test???.str}",
            new ArrayList<>(Arrays.asList(BuiltinLibraryIdentifier.StrategoLib)));
    }

    @Disabled("To be enabled when the bugs found by these tests are removed")
    @TestFactory
    Stream<DynamicTest> test2() throws URISyntaxException, IOException {
        return compileAndRun("test2", "*.str",
            new ArrayList<>(Arrays.asList(BuiltinLibraryIdentifier.StrategoLib)));
    }

    @Disabled("To be enabled when the bugs found by these tests are removed")
    @TestFactory
    Stream<DynamicTest> testNeg() throws URISyntaxException, IOException {
        return failToCompile("testneg", "test*.str",
            new ArrayList<>(Arrays.asList(BuiltinLibraryIdentifier.StrategoLib)));
    }

    protected Stream<DynamicTest> compileAndRun(String subdir, String glob,
        ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws URISyntaxException, IOException {
        final Path strategoxtJarPath = Stratego.getStrategoxtJarPath();
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
                final CompileOutput str2CompileOutput =
                    Stratego.str2(p, baseName, packageName, packageDir, false, linkedLibraries, false);
                Assertions.assertTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors: "
                        + ((CompileOutput.Failure) str2CompileOutput).messages);
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
                Assertions.assertTrue(Stratego.str2(p, baseName, packageName, packageDir, true,
                    linkedLibraries, false) instanceof CompileOutput.Failure,
                    "Compilation with stratego.lang compiler expected to fail");
            });
        });
    }

    @SuppressWarnings("SameParameterValue")
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
                final CompileOutput str2CompileOutput =
                    Stratego.str2(p, baseName, packageName, packageDir, true, linkedLibraries, false);
                Assertions.assertTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors: "
                        + ((CompileOutput.Failure) str2CompileOutput).messages);
            });
        });
    }

    private Stream<Path> streamStrategoFiles(Path dirWithTestFiles, String glob)
        throws IOException {
        final PathMatcher matcher =
            dirWithTestFiles.getFileSystem().getPathMatcher("glob:**/" + glob);
        return Files.list(dirWithTestFiles).filter(matcher::matches);
    }

    protected Path getResourcePathRoot() throws URISyntaxException {
        return Paths.get(this.getClass().getResource("/").toURI());
    }

    private static Iterable<? extends File> javaFiles(CompileOutput.Success str2CompileOutput) {
        final HashSet<ResourcePath> resultFiles = str2CompileOutput.resultFiles;
        final List<File> sourceFiles = new ArrayList<>(resultFiles.size());
        for(ResourcePath resultFile : resultFiles) {
            sourceFiles.add(resourceService.toLocalFile(resultFile));
        }
        return sourceFiles;
    }

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
