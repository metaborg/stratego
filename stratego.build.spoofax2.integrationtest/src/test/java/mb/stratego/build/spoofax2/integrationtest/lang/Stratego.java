package mb.stratego.build.spoofax2.integrationtest.lang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.strj.strj;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.Task;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.store.SerializingStore;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.spoofax2.StrIncrModule;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.CompileOutput;

public class Stratego {
    public static Path getStrategoxtJarPath() {
        return Paths.get(System.getProperty("strategoxt-jar"));
    }

    public static Path getStrategoPath() {
        return Paths.get(System.getProperty("omml-stratego"));
    }

    public static Path getStratego2Path() {
        return Paths.get(System.getProperty("stratego-lang"));
    }

    static boolean strj(Path input, String baseName, String packageName, Path packageDir) {
        strj.init();
        final IStrategoTerm result;
        try {
            //@formatter:off
            result = strj.mainNoExit(
                "-i", input.toString(),
                "-o", packageDir.resolve("Main.java").toString(),
                "-p", packageName,
                "-la", "stratego-lib",
                "-D", "VERSION_TERM=\"${version}\"",
                "-D", "SVN_REVISION_TERM=\"${revision}\"",
//                "-I", "../../src/main/strategies",
//                "-I", "../../src/main/strategies/ssl-compat",
                "-m", "main-" + baseName,
                "--verbose", "error"
            );
            //@formatter:on
        } catch(StrategoExit exit) {
            return exit.getValue() == 0;
        }
        return result != null;
    }

    public static CompileOutput str2(Path input, String baseName, String packageName,
        Path packageDir, boolean library,
        ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries, boolean autoImportStd)
        throws MetaborgException, IOException {
        final Path temporaryDirectoryPath =
            Files.createTempDirectory("mb.stratego.build.spoofax2.integrationtest")
                .toAbsolutePath();

        try(Spoofax spoofax = new Spoofax(new StrIncrModule(), new GuiceTaskDefsModule())) {
            // compile

            final FSPath serializingStorePath =
                new FSPath(temporaryDirectoryPath.resolve("pie-store"));

            // load Stratego language for later discovery during compilation (parsing in particular)
            spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(Stratego.getStrategoPath().toFile()));
            spoofax.languageDiscoveryService
                .languageFromArchive(spoofax.resolve(Stratego.getStratego2Path().toFile()));

            final PieBuilder pieBuilder = new PieBuilderImpl();
            pieBuilder.withStoreFactory(
                (serde, resourceService, loggerFactory) -> new SerializingStore<>(serde,
                    resourceService.getWritableResource(serializingStorePath), InMemoryStore::new,
                    InMemoryStore.class));
            pieBuilder.withTaskDefs(spoofax.injector.getInstance(GuiceTaskDefs.class));
            Pie pie = pieBuilder.build();

            final ResourcePath projectPath = new FSPath(input.getParent());

            if(!linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib)) {
                linkedLibraries.add(BuiltinLibraryIdentifier.StrategoLib);
            }
            final ArrayList<ResourcePath> strjIncludeDirs = new ArrayList<>(1);
            strjIncludeDirs.add(projectPath);

            final Arguments newArgs = new Arguments();
            final ModuleIdentifier mainModuleIdentifier =
                new ModuleIdentifier(input.getFileName().toString().endsWith(".str"), false, baseName, new FSPath(input));
            CompileInput compileInput =
                new CompileInput(mainModuleIdentifier, projectPath, new FSPath(packageDir),
                    packageName, new FSPath(temporaryDirectoryPath.resolve("cacheDir")),
                    new ArrayList<>(0), strjIncludeDirs, linkedLibraries, newArgs,
                    new ArrayList<>(0), library, autoImportStd);
            Task<CompileOutput> compileTask =
                spoofax.injector.getInstance(Compile.class).createTask(compileInput);

            try(final MixedSession session = pie.newSession()) {
                CompileOutput result = Objects.requireNonNull(session.require(compileTask));
                return result;
            } catch(ExecException e) {
                throw new MetaborgException("Incremental Stratego build failed: " + e.getMessage(),
                    e);
            } catch(InterruptedException e) {
                throw new MetaborgException(
                    "Incremental Stratego build interrupted: " + e.getMessage(), e);
            }
        }
    }
}
