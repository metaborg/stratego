package mb.stratego.build.spoofax2.integrationtest;

import com.google.common.collect.Lists;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.stratego.build.spoofax2.integrationtest.lang.Java;
import mb.stratego.build.spoofax2.integrationtest.lang.Stratego;
import mb.stratego.build.spoofax2.integrationtest.util.NaturalOrderComparator;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.*;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.util.cmd.Arguments;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static mb.stratego.build.spoofax2.integrationtest.StrcTests.getErrorMessagesString;
import static mb.stratego.build.spoofax2.integrationtest.StrcTests.javaFiles;

public class ParameterisedStratego2Tests {
    public static final String packageName = "mb.stratego2integrationtest";
    public static final String packageDirName = packageName.replace('.', '/');
    public static final ResourceService resourceService =
            new DefaultResourceService(new FSResourceRegistry());

    final List<Arguments> argParams = Lists.newArrayList(
            ArgumentsFactory("-O", "2"),
//            ArgumentsFactory("-O", "3"),
            ArgumentsFactory("-O", "4")
    );

    @TestFactory
    DynamicNode parameterisedTests() throws URISyntaxException, IOException {
        return DynamicContainer.dynamicContainer("Parameterised tests", Arrays.asList(
                DynamicContainer.dynamicContainer("test1", test1()),
                DynamicContainer.dynamicContainer("test2", test2())
        ));
    }

    private Stream<DynamicNode> test1() throws URISyntaxException, IOException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        HashSet<String> disabledTestFiles =
                new HashSet<>(Arrays.asList("test113.str2", "test-libstrc.str2"));
        final Predicate<Path> disableFilter =
                p -> !disabledTestFiles.contains(p.getFileName().toString())
                        && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        final Path dirWithTestFiles = getResourcePathRoot().resolve("test1");
        return streamStrategoFiles(dirWithTestFiles, "test*.str*", disableFilter)
                .sorted(new NaturalOrderComparator<>())
                .map(p -> DynamicContainer.dynamicContainer(p.getFileName().toString(), argParams
                        .stream()
                        .map(args -> compileAndRun(p, Arrays.asList(BuiltinLibraryIdentifier.StrategoLib,
                                BuiltinLibraryIdentifier.StrategoSdf), args))));
    }

    @TestFactory
    private Stream<DynamicNode> failingTests() throws URISyntaxException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        Path test1 = getResourcePathRoot().resolve("test1");
        Path test2 = getResourcePathRoot().resolve("test2");

        List<String> test1Names = Arrays.asList("test33", "test34", "test53", "test112");
        List<String> test2Names = Arrays.asList("occan");
//        List<String> test1Names = Collections.EMPTY_LIST;
//        List<String> test2Names = Collections.EMPTY_LIST;

        return Stream
                .concat(
                        test1Names.stream().map(s -> test1.resolve(s + ".str2")),
                        test2Names.stream().map(s -> test2.resolve(s + ".str2"))
                ).map(p -> DynamicContainer.dynamicContainer(p.getFileName().toString(),
                        argParams
                            .stream()
                            .map(args -> compileAndRun(p, Arrays.asList(BuiltinLibraryIdentifier.StrategoLib, BuiltinLibraryIdentifier.StrategoSdf), args)))
                );
    }

    @TestFactory
    private Stream<DynamicNode> test2() throws URISyntaxException, IOException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        // list-cons is not a test file, it is imported by other test files.
        HashSet<String> disabledTestFiles =
                new HashSet<>(Collections.singletonList("list-cons.str2"));
        final Predicate<Path> disableFilter =
                p -> !disabledTestFiles.contains(p.getFileName().toString())
                        && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        final Path dirWithTestFiles = getResourcePathRoot().resolve("test2");
        return streamStrategoFiles(dirWithTestFiles, "*.str*", disableFilter)
                .sorted(new NaturalOrderComparator<>())
                .map(p -> DynamicContainer.dynamicContainer(p.getFileName().toString(), argParams
                        .stream()
                        .map(args -> compileAndRun(p, Collections.singletonList(BuiltinLibraryIdentifier.StrategoLib), args))));
    }

    private static Stream<Path> streamStrategoFiles(Path dirWithTestFiles, String glob, Predicate<Path> disabled)
            throws IOException {
        final PathMatcher matcher =
                dirWithTestFiles.getFileSystem().getPathMatcher("glob:**/" + glob);

        return Files.list(dirWithTestFiles).filter(matcher::matches).filter(disabled);
    }

    private DynamicTest compileAndRun(Path filepath,
                                        List<IModuleImportService.ModuleIdentifier> linkedLibraries,
                                        Arguments args) {
        final Path strategoxtJarPath = Stratego.getStrategoxtJarPath();
        final String fileName = filepath.getFileName().toString();
        final String baseName = FilenameUtils.removeExtension(fileName);
        final Path testGenDir = filepath.resolveSibling(baseName + "/test-gen");
        final Path packageDir = testGenDir.resolve(packageDirName);
        final LanguageIdentifier languageIdentifier =
                new LanguageIdentifier("mb.stratego", "compnrun_" + baseName,
                        new LanguageVersion(1));
        return DynamicTest.dynamicTest(String.format("Compile & run %s (%s)", baseName, args), () -> {
            FileUtils.deleteDirectory(testGenDir.toFile());
            Files.createDirectories(packageDir);
            final CompileOutput str2CompileOutput = Stratego.str2(filepath, baseName, packageName, packageDir, false, new ArrayList<>(linkedLibraries), false, languageIdentifier, args);
            Assumptions.assumeTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors:\n"
                            + getErrorMessagesString(str2CompileOutput));
            Iterable<? extends File> sourceFiles = javaFiles((CompileOutput.Success) str2CompileOutput);
            Assumptions.assumeTrue(Java.compile(testGenDir, sourceFiles,
                    Collections.singletonList(strategoxtJarPath.toFile())),
                    "Compilation with javac expected to succeed");
            Assertions.assertTrue(
                    Java.execute(testGenDir + ":" + strategoxtJarPath, packageName + ".Main"),
                    "Running java expected to succeed");
        });
    }

    private Path getResourcePathRoot() throws URISyntaxException {
        return Paths.get(this.getClass().getResource("/").toURI());
    }

    private static Arguments ArgumentsFactory(Object... args) {
        return new Arguments().add(args);
    }

}
