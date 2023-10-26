package mb.stratego.build.spoofax2.integrationtest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.build.SpoofaxCommonPaths;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingFacet;
import org.metaborg.util.cmd.Arguments;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.api.ValueSupplier;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.SerializingStoreBuilder;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.spoofax2.StrIncrModule;
import mb.stratego.build.spoofax2.integrationtest.lang.Stratego;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.CompileOutput;

public class StrategoIncrementalCompilationTest {
    public static @Nullable Spoofax spoofax;

    @BeforeAll static void createSpoofaxInstance() throws MetaborgException {
        spoofax = new Spoofax(new StrIncrModule(), new GuiceTaskDefsModule());
    }

    @Test public void testCompile() throws IOException, MetaborgException {
        // setup
        assert spoofax != null : "@BeforeAll should set up Spoofax instance";

        final Path temporaryDirectoryPath =
            Files.createTempDirectory(StrategoIncrementalCompilationTest.class.getName())
                .toAbsolutePath();

        final Path helloFile = temporaryDirectoryPath.resolve("hello.str2");
        final String helloFileContents =
            "module hello " + "imports " + "  strategolib" + "  world " + "rules "
                + "  hello = !$[Hello, [<world>]]; debug";

        Files.write(helloFile, helloFileContents.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        final Path worldFile = temporaryDirectoryPath.resolve("world.str2");
        final String worldFileContents =
            "module world " + "imports " + "  strategolib " + "rules "
                + "  world = !\"world!\"";

        Files.write(worldFile, worldFileContents.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // compile

        final FSPath serializingStorePath = new FSPath(temporaryDirectoryPath.resolve("pie-store"));

        // load Stratego languages for later discovery during compilation (parsing in particular)
        spoofax.languageDiscoveryService
            .languageFromArchive(spoofax.resolve(Stratego.getStrategoPath().toFile()));
        spoofax.languageDiscoveryService
            .languageFromArchive(spoofax.resolve(Stratego.getStratego2Path().toFile()));

        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withStoreFactory(
            (serde, resourceService, loggerFactory) ->
                SerializingStoreBuilder
                    .ofInMemoryStore(serde)
                    .withResourceStorage(resourceService.getWritableResource(serializingStorePath))
                    .build());
        pieBuilder.withTaskDefs(spoofax.injector.getInstance(GuiceTaskDefs.class));
        Pie pie = pieBuilder.build();

        final File projectLocation = temporaryDirectoryPath.toFile();
        final ResourcePath projectPath = new FSPath(projectLocation);

        final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries = new ArrayList<>(1);
        linkedLibraries.add(BuiltinLibraryIdentifier.StrategoLib);
        final LinkedHashSet<ResourcePath> strjIncludeDirs = new LinkedHashSet<>();
        strjIncludeDirs.add(new FSPath(projectLocation));

        final Arguments newArgs = new Arguments();
        final String mainModuleName = "hello";
        final ModuleIdentifier mainModuleIdentifier =
            new ModuleIdentifier(true, false, mainModuleName, new FSPath(helloFile));
        Path depPath = temporaryDirectoryPath.resolve("depPath");
        final String libraryName = "incrCompTest";
        final ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add("mb.stratego.build.spoofax2.test");
        final ResourcePath str2libReplicateDir = projectPath.appendOrReplaceWithPath("target/replicate/str2libs");
        final LinkedHashSet<Supplier<Stratego2LibInfo>> str2libraries = new LinkedHashSet<>(1);
        {
            // load strategolib language (str2lib)
            final ILanguageImpl sourceDepImpl = spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(Stratego.getStrategoLibPath().toFile()));
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
            new CompileInput(mainModuleIdentifier, projectPath, new FSPath(depPath), str2libReplicateDir,
                packageNames,
                new FSPath(temporaryDirectoryPath.resolve("cacheDir")), new ArrayList<>(0),
                strjIncludeDirs, linkedLibraries, newArgs, new ArrayList<>(0), true, true, true,
                libraryName, str2libraries, true, true, null);
        Task<CompileOutput> compileTask =
            spoofax.injector.getInstance(Compile.class).createTask(compileInput);

        try(final MixedSession session = pie.newSession()) {
            session.require(compileTask);
        } catch(ExecException e) {
            throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            // Ignore
        }

        Path strategoJavaPackageOutputDir = depPath.resolve(packageNames.get(0).replace('.','/'));
        Assertions.assertTrue(Files.exists(strategoJavaPackageOutputDir));
        Assertions.assertTrue(Files.isDirectory(strategoJavaPackageOutputDir));
        final Path interopRegistererJavaFile = strategoJavaPackageOutputDir.resolve("InteropRegisterer.java");
        Assertions.assertTrue(Files.exists(interopRegistererJavaFile));
        Assertions.assertTrue(Files.isRegularFile(interopRegistererJavaFile));
        Assertions.assertTrue(new String(Files.readAllBytes(interopRegistererJavaFile), StandardCharsets.UTF_8).contains("InteropRegisterer"));
        final Path mainJavaFile = strategoJavaPackageOutputDir.resolve("Main.java");
        Assertions.assertTrue(Files.exists(mainJavaFile));
        Assertions.assertTrue(Files.isRegularFile(mainJavaFile));
        Assertions.assertTrue(new String(Files.readAllBytes(mainJavaFile), StandardCharsets.UTF_8).contains("Main"));
        final Path mainStrategyJavaFile = strategoJavaPackageOutputDir.resolve("hello_0_0.java");
        Assertions.assertTrue(Files.exists(mainStrategyJavaFile));
        Assertions.assertTrue(Files.isRegularFile(mainStrategyJavaFile));
        Assertions.assertTrue(new String(Files.readAllBytes(mainStrategyJavaFile), StandardCharsets.UTF_8).contains("hello_0_0"));
        final Path worldStrategyJavaFile = strategoJavaPackageOutputDir.resolve("world_0_0.java");
        Assertions.assertTrue(Files.exists(worldStrategyJavaFile));
        Assertions.assertTrue(Files.isRegularFile(worldStrategyJavaFile));
        Assertions.assertTrue(new String(Files.readAllBytes(worldStrategyJavaFile), StandardCharsets.UTF_8).contains("world_0_0"));
        final Path testPackageJavaFile = strategoJavaPackageOutputDir.resolve("test.java");
        Assertions.assertTrue(Files.exists(testPackageJavaFile));
        Assertions.assertTrue(Files.isRegularFile(testPackageJavaFile));
        Assertions.assertTrue(new String(Files.readAllBytes(testPackageJavaFile), StandardCharsets.UTF_8).contains("test"));

        // serialize

        pie.close();

        // deserialize

        pie = pieBuilder.build();

        // incremental change

        final String newHelloFileContents =
            "module hello " + "imports " + "  strategolib " + "  world " + "rules "
                + "  hello = !$[Hello, [<world>]]; debug; rules(A: (a, \"hi\") -> (a, \"no\"))";

        Files.write(helloFile, newHelloFileContents.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        depPath = temporaryDirectoryPath.resolve("depPath2");

        linkedLibraries.add(BuiltinLibraryIdentifier.StrategoAterm);

        compileInput =
            new CompileInput(mainModuleIdentifier, projectPath, new FSPath(depPath), str2libReplicateDir,
                packageNames,
                new FSPath(temporaryDirectoryPath.resolve("cacheDir2")), new ArrayList<>(0),
                strjIncludeDirs, linkedLibraries, newArgs, new ArrayList<>(0), true, true, true,
                libraryName, str2libraries, true, true, null);

        compileTask = spoofax.injector.getInstance(Compile.class).createTask(compileInput);

        // compile bottom-up

        try(final MixedSession session = pie.newSession()) {
            TopDownSession tdSession = session.updateAffectedBy(Collections.singleton(new FSPath(helloFile)));
            session.deleteUnobservedTasks(t -> true,
                (t, r) -> r != null && Objects.equals(r.getLeafExtension(), "java"));

            // compile in different place
            final CompileOutput compileOutput =
                Objects.requireNonNull(tdSession.require(compileTask));
            if(compileOutput instanceof CompileOutput.Failure) {
                final CompileOutput.Failure failure = (CompileOutput.Failure) compileOutput;
                int errorsCount = 0;
                for(Message message : failure.messages) {
                    if(message.severity == MessageSeverity.ERROR) {
                        errorsCount++;
                    }
                }
                throw new MetaborgException(
                    "Incremental Stratego Compilation failed with " + errorsCount + " errors.");
            } else {
                assert compileOutput instanceof CompileOutput.Success;
            }
        } catch(ExecException e) {
            throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            // Ignore
        }
    }

    @AfterAll public static void destroySpoofaxInstance() {
        assert
            spoofax != null : "@Before should set up Spoofax instance, test should not remove it";
        spoofax.close();
        spoofax = null;
    }
}
