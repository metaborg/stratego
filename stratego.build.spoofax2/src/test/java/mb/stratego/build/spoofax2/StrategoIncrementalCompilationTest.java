package mb.stratego.build.spoofax2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.util.cmd.Arguments;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.store.SerializingStore;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.CompileOutput;
import mb.stratego.build.util.StrategoGradualSetting;

import static org.junit.Assert.assertTrue;

public class StrategoIncrementalCompilationTest {
    public @Nullable Spoofax spoofax;

    @Before public void createSpoofaxInstance() throws MetaborgException {
        spoofax = new Spoofax(new StrIncrModule(), new GuiceTaskDefsModule());
    }

    @Test public void testCompile() throws IOException, MetaborgException {
        // setup
        assert spoofax != null : "@Before should set up Spoofax instance";

        final Path temporaryDirectoryPath =
            Files.createTempDirectory(StrategoIncrementalCompilationTest.class.getName())
                .toAbsolutePath();

        final Path helloFile = temporaryDirectoryPath.resolve("hello.str");
        final String helloFileContents =
            "module hello " + "imports " + "  libstratego-lib " + "  world " + "rules "
                + "  hello = !$[Hello, [<world>]]; debug";

        Files.write(helloFile, helloFileContents.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        final Path worldFile = temporaryDirectoryPath.resolve("world.str");
        final String worldFileContents =
            "module world " + "imports " + "  libstratego-lib " + "rules "
                + "  world = !\"world!\"";

        Files.write(worldFile, worldFileContents.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // compile

        final FSPath serializingStorePath = new FSPath(temporaryDirectoryPath.resolve("pie-store"));

        // load Stratego language for later discovery during compilation (parsing in particular)
        final URL strategoURL =
            getClass().getResource("/stratego.spoofax-language");
        spoofax.languageDiscoveryService
            .languageFromArchive(spoofax.resolve(strategoURL.getFile()));

        final PieBuilder pieBuilder = new PieBuilderImpl();
        pieBuilder.withStoreFactory((logger, resourceService) -> new SerializingStore<>(
            resourceService.getWritableResource(serializingStorePath), new InMemoryStore()));
        pieBuilder.withTaskDefs(spoofax.injector.getInstance(GuiceTaskDefs.class));
        Pie pie = pieBuilder.build();

        final File projectLocation = temporaryDirectoryPath.toFile();

        final ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries = new ArrayList<>(1);
        linkedLibraries.add(BuiltinLibraryIdentifier.StrategoLib);
        final ArrayList<ResourcePath> strjIncludeDirs = new ArrayList<>(1);
        strjIncludeDirs.add(new FSPath(projectLocation));

        final Arguments newArgs = new Arguments();
        final String mainModuleName = "hello";
        final ModuleIdentifier mainModuleIdentifier =
            new ModuleIdentifier(false, mainModuleName, new FSPath(helloFile));
        Path depPath = temporaryDirectoryPath.resolve("depPath");
        CompileInput compileInput = new CompileInput(mainModuleIdentifier, new FSPath(depPath),
            "mb.stratego.build.spoofax2.test",
            new FSPath(temporaryDirectoryPath.resolve("cacheDir")), new ArrayList<>(0),
            strjIncludeDirs, linkedLibraries, newArgs, new ArrayList<>(0),
            StrategoGradualSetting.DYNAMIC);
        Task<CompileOutput> compileTask =
            spoofax.injector.getInstance(Compile.class).createTask(compileInput);

        try(final MixedSession session = pie.newSession()) {
            session.require(compileTask);
        } catch(ExecException e) {
            throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            // Ignore
        }

        Path strategoJavaPackageOutputDir = depPath;
        assertTrue(Files.exists(strategoJavaPackageOutputDir));
        assertTrue(Files.isDirectory(strategoJavaPackageOutputDir));
        final Path interopRegistererJavaFile = strategoJavaPackageOutputDir.resolve("InteropRegisterer.java");
        assertTrue(Files.exists(interopRegistererJavaFile));
        assertTrue(Files.isRegularFile(interopRegistererJavaFile));
        assertTrue(new String(Files.readAllBytes(interopRegistererJavaFile), StandardCharsets.UTF_8).contains("InteropRegisterer"));
        final Path mainJavaFile = strategoJavaPackageOutputDir.resolve("Main.java");
        assertTrue(Files.exists(mainJavaFile));
        assertTrue(Files.isRegularFile(mainJavaFile));
        assertTrue(new String(Files.readAllBytes(mainJavaFile), StandardCharsets.UTF_8).contains("Main"));
        final Path mainStrategyJavaFile = strategoJavaPackageOutputDir.resolve("hello_0_0.java");
        assertTrue(Files.exists(mainStrategyJavaFile));
        assertTrue(Files.isRegularFile(mainStrategyJavaFile));
        assertTrue(new String(Files.readAllBytes(mainStrategyJavaFile), StandardCharsets.UTF_8).contains("hello_0_0"));
        final Path worldStrategyJavaFile = strategoJavaPackageOutputDir.resolve("world_0_0.java");
        assertTrue(Files.exists(worldStrategyJavaFile));
        assertTrue(Files.isRegularFile(worldStrategyJavaFile));
        assertTrue(new String(Files.readAllBytes(worldStrategyJavaFile), StandardCharsets.UTF_8).contains("world_0_0"));
        final Path testPackageJavaFile = strategoJavaPackageOutputDir.resolve("test.java");
        assertTrue(Files.exists(testPackageJavaFile));
        assertTrue(Files.isRegularFile(testPackageJavaFile));
        assertTrue(new String(Files.readAllBytes(testPackageJavaFile), StandardCharsets.UTF_8).contains("test"));

        // serialize

        pie.close();

        // deserialize

        pie = pieBuilder.build();

        // incremental change

        final String newHelloFileContents =
            "module hello " + "imports " + "  libstratego-lib " + "  world " + "rules "
                + "  hello = !$[Hello, [<world>]]; debug";

        Files.write(helloFile, newHelloFileContents.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        depPath = temporaryDirectoryPath.resolve("depPath2");

        linkedLibraries.add(BuiltinLibraryIdentifier.StrategoAterm);

        compileInput = new CompileInput(mainModuleIdentifier, new FSPath(depPath),
            "mb.stratego.build.spoofax2.test",
            new FSPath(temporaryDirectoryPath.resolve("cacheDir2")), new ArrayList<>(0),
            strjIncludeDirs, linkedLibraries, newArgs, new ArrayList<>(0),
            StrategoGradualSetting.STATIC);

        compileTask = spoofax.injector.getInstance(Compile.class).createTask(compileInput);

        // compile

        try(final MixedSession session = pie.newSession()) {
            session.require(compileTask);
            session.deleteUnobservedTasks(t -> true,
                (t, r) -> r instanceof HierarchicalResource && Objects
                    .equals(((HierarchicalResource) r).getLeafExtension(), "java"));

            final CompileOutput compileOutput =
                Objects.requireNonNull(session.require(compileTask));
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

    @After public void destroySpoofaxInstance() {
        assert
            spoofax != null : "@Before should set up Spoofax instance, test should not remove it";
        spoofax.close();
        spoofax = null;
    }
}
