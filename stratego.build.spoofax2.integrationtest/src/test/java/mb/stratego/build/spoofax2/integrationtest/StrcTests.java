package mb.stratego.build.spoofax2.integrationtest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import mb.stratego.junit_bridge.Java;
import mb.stratego.junit_bridge.StrTests;

@Disabled("Preloading the stratego.lang language doesn't seem to work")
public class StrcTests extends StrTests {
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
        final Path strjUnderTest = getStrjUnderTest();
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
                    Collections.singletonList(strjUnderTest.toFile())),
                    "Compilation with javac expected to succeed");
                Assertions.assertTrue(
                    Java.execute(testGenDir + ":" + strjUnderTest, packageName + ".Main"),
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
}
