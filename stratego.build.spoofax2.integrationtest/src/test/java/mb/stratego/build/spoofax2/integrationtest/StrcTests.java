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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import mb.stratego.build.spoofax2.integrationtest.lang.Java;
import mb.stratego.build.spoofax2.integrationtest.lang.Stratego;

@Disabled("Preloading the stratego.lang language doesn't seem to work")
public class StrcTests {
    public static final String packageName = "mb.stratego2integrationtest";
    public static final String packageDirName = packageName.replace('.', '/');

    @TestFactory Stream<DynamicTest> test1() throws URISyntaxException, IOException {
        return compileAndRun("test1", "test???.str");
    }

    @TestFactory Stream<DynamicTest> test2() throws URISyntaxException, IOException {
        return compileAndRun("test2", "*.str");
    }

    @TestFactory Stream<DynamicTest> testStrc() throws URISyntaxException, IOException {
        return compileAndRun("test-strc", "{test??.str,test???.str}");
    }

    @TestFactory Stream<DynamicTest> testNeg() throws URISyntaxException, IOException {
        return failToCompile("testneg", "test*.str");
    }

    protected Stream<DynamicTest> compileAndRun(String subdir, String glob)
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
                Assertions.assertTrue(Stratego.str2(p, baseName, packageName, packageDir),
                    "Compilation with strj expected to succeed");
                Assertions.assertTrue(Java.compile(testGenDir, javaFiles(packageDir),
                    Collections.singletonList(strategoxtJarPath.toFile())),
                    "Compilation with javac expected to succeed");
                Assertions.assertTrue(
                    Java.execute(testGenDir + ":" + strategoxtJarPath, packageName + ".Main"),
                    "Running java expected to succeed");
            });
        });
    }

    @SuppressWarnings("SameParameterValue")
    protected Stream<DynamicTest> failToCompile(String subdir, String glob)
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
                Assertions.assertFalse(Stratego.str2(p, baseName, packageName, packageDir),
                    "Compilation with strj expected to fail");
            });
        });
    }

    private Stream<Path> streamStrategoFiles(Path dirWithTestFiles, String glob)
        throws IOException {
        final PathMatcher matcher = dirWithTestFiles.getFileSystem().getPathMatcher("glob:**/" + glob);
        return Files.list(dirWithTestFiles).filter(matcher::matches);
    }

    protected Path getResourcePathRoot() throws URISyntaxException {
        return Paths.get(this.getClass().getResource("/").toURI());
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
