package mb.stratego.build.spoofax2.integrationtest;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static mb.stratego.build.spoofax2.integrationtest.StrcTests.getErrorMessagesString;
import static mb.stratego.build.spoofax2.integrationtest.StrcTests.javaFiles;

public class ParameterisedStratego2Tests {
    public static final String packageName = "mb.stratego2paramtest";
    public static final String packageDirName = packageName.replace('.', '/');
    public static final ResourceService resourceService =
            new DefaultResourceService(new FSResourceRegistry());

    final List<Arguments> argParams = Arrays.asList(
//            ArgumentsFactory("-O", "2"),
//            ArgumentsFactory("-O", "4", "--pmc:switchv", "elseif"),
            ArgumentsFactory("-O", "4")
    );

    @TestFactory
    DynamicNode parameterisedTests() throws URISyntaxException, IOException {
        return DynamicContainer.dynamicContainer("Parameterised tests", Arrays.asList(
                DynamicContainer.dynamicContainer("Failing tests", failingTests())
//                , DynamicContainer.dynamicContainer("MultiMatch tests", testPMC())
                , DynamicContainer.dynamicContainer("test1", test1())
                , DynamicContainer.dynamicContainer("test2", test2())
        ));
    }

    @TestFactory
    private Stream<DynamicNode> test1() throws URISyntaxException, IOException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        HashSet<String> disabledTestFiles =
                new HashSet<>(Arrays.asList("test113.str2", "test94.str2", "test105.str2"));
        final Predicate<Path> disableFilter =
                p -> !disabledTestFiles.contains(p.getFileName().toString())
                        && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        final Path dirWithTestFiles = getResourcePathRoot().resolve("test1");
        return streamStrategoFiles(dirWithTestFiles, "{test??.str2,test???.str2}", disableFilter)
                .sorted(new NaturalOrderComparator<>())
                .map(p -> DynamicContainer.dynamicContainer(p.getFileName().toString(), argParams
                        .stream()
                        .map(args -> compileAndRun(p, Collections.singletonList(BuiltinLibraryIdentifier.StrategoSdf), args))));
    }

    @TestFactory
    private Stream<DynamicNode> failingTests() throws URISyntaxException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        Path test1 = getResourcePathRoot().resolve("test1");
        Path test2 = getResourcePathRoot().resolve("test2");

        List<String> test1Names = Arrays.asList("test31", "test33", "test34", "test37", "test53", "test76", "test77", "test78", "test87", "test90", "test92", "test93", "test98", "test112");
        List<String> test2Names = Arrays.asList("occan", "traversal-test");

        return Stream
                .concat(
                        test2Names.stream().map(s -> test2.resolve(s + ".str2")),
                        test1Names.stream().map(s -> test1.resolve(s + ".str2"))
                ).map(p -> DynamicContainer.dynamicContainer(p.getFileName().toString(),
                        argParams
                            .stream()
                            .map(args -> compileAndRun(p, Collections.singletonList(BuiltinLibraryIdentifier.StrategoSdf), args)))
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
        return streamStrategoFiles(dirWithTestFiles, "*.str2", disableFilter)
                .sorted(new NaturalOrderComparator<>())
                .map(p -> {
                    System.out.println(p);
                    return DynamicContainer.dynamicContainer(p.getFileName().toString(), argParams
                        .stream()
                        .map(args -> compileAndRun(p, Collections.emptyList(), args)));
                });
    }

    @TestFactory
    @Disabled(value = "testing PMC as end-to-end tests doesn't make sense anymore as these are now rejected in the surface syntax during static analysis")
    private Stream<DynamicNode> testPMC() throws URISyntaxException, IOException {
        // test113 tests that tabs are considered 4 spaces wide by string quotations.
        //   This is currently not easy to support with post-processing, and we don't want to add
        //   a hack specific to the Stratego grammar in there. The post-processing method therefore
        //   works best when using spaces as indentation in Stratego files.
        // list-cons is not a test file, it is imported by other test files.
        HashSet<String> disabledTestFiles =
                new HashSet<>(Collections.emptySet());
        final Predicate<Path> disableFilter =
                p -> !disabledTestFiles.contains(p.getFileName().toString())
                        && !(p.getFileName().toString().contains(".core") || p.getFileName().toString().contains(".opt"));
        final Path dirWithTestFiles = getResourcePathRoot().resolve("test-pmc");
        return streamStrategoFiles(dirWithTestFiles, "*.str2", disableFilter)
                .sorted(new NaturalOrderComparator<>())
                .map(p -> DynamicContainer.dynamicContainer(p.getFileName().toString(), argParams
                        .stream()
                        .map(args -> compileAndRun(p, Collections.emptyList(), args))));
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
        final Path strategoLibJarPath = Stratego.getStrategLibJarPath();
        final String fileName = filepath.getFileName().toString();
        final String baseName = FilenameUtils.removeExtension(fileName);
        final Path outputDir = filepath.resolveSibling(baseName + "/test-gen");
//        final Path packageDir = testGenDir.resolve(packageDirName);
        final LanguageIdentifier languageIdentifier =
                new LanguageIdentifier("mb.stratego", "compnrun_" + baseName,
                        new LanguageVersion(1));
        return DynamicTest.dynamicTest(String.format("Compile & run %s (%s)", baseName, args), () -> {
            FileUtils.deleteDirectory(outputDir.toFile());
            Files.createDirectories(outputDir);
            final CompileOutput str2CompileOutput = Stratego.str2(filepath, baseName, packageName, outputDir, false, new ArrayList<>(linkedLibraries), false, languageIdentifier, args);
            Assumptions.assumeTrue(str2CompileOutput instanceof CompileOutput.Success, () ->
                    "Compilation with stratego.lang compiler expected to succeed, but gave errors:\n"
                            + getErrorMessagesString(str2CompileOutput));
            Iterable<? extends File> sourceFiles = javaFiles((CompileOutput.Success) str2CompileOutput);
            Assumptions.assumeTrue(Java.compile(outputDir, sourceFiles,
                    Arrays.asList(outputDir.toFile(), strategoLibJarPath.toFile(), strategoxtJarPath.toFile())),
                    "Compilation with javac expected to succeed");
            Assertions.assertTrue(
                    Java.execute(Arrays.asList(outputDir, strategoLibJarPath, strategoxtJarPath), packageName + ".Main"),
                    "Running java expected to succeed (" + baseName + ")");
        });
    }

    private Path getResourcePathRoot() throws URISyntaxException {
        return Paths.get(this.getClass().getResource("/").toURI());
    }

    private static Arguments ArgumentsFactory(Object... args) {
        return new Arguments().add(args);
    }

}
