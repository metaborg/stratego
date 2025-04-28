package mb.stratego.build.spoofax2.integrationtest.lang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingFacet;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.strj.strj;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.ValueSupplier;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.SerializingStoreBuilder;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.spoofax2.StrIncrModule;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.CompileOutput;

public class Stratego {
    public static Path getStrategoxtJarPath() {
        return Paths.get(System.getProperty("strategoxt-jar"));
    }

    public static Path getStrategLibJarPath() {
        return Paths.get(System.getProperty("strategolib-jar"));
    }

    public static Path getStrategoPath() {
        return Paths.get(System.getProperty("omml-stratego"));
    }

    public static Path getStratego2Path() {
        return Paths.get(System.getProperty("stratego-lang"));
    }

    public static Path getStrategoLibPath() {
        return Paths.get(System.getProperty("strategolib"));
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
                                     ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries, boolean autoImportStd,
                                     LanguageIdentifier languageIdentifier)
        throws MetaborgException, IOException {
        return str2(input, baseName, packageName, packageDir, library, linkedLibraries, autoImportStd, languageIdentifier, new Arguments());
    }

    public static CompileOutput str2(Path input, String baseName, String packageName,
        Path outputDir, boolean library,
        ArrayList<IModuleImportService.ModuleIdentifier> linkedLibraries, boolean autoImportStd,
        LanguageIdentifier languageIdentifier, Arguments args)
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
                (serde, resourceService, loggerFactory) ->
                    SerializingStoreBuilder
                        .ofInMemoryStore(serde)
                        .withResourceStorage(resourceService.getWritableResource(serializingStorePath))
                        .build());
            pieBuilder.withTaskDefs(spoofax.injector.getInstance(GuiceTaskDefs.class));
            Pie pie = pieBuilder.build();

            final ResourcePath projectPath = new FSPath(input.getParent());

            final LinkedHashSet<Supplier<Stratego2LibInfo>> str2libraries = new LinkedHashSet<>();
            if(!linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib)) {
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
            final LinkedHashSet<ResourcePath> strjIncludeDirs = new LinkedHashSet<>();
            strjIncludeDirs.add(projectPath);
            final ArrayList<String> packageNames = new ArrayList<>(1);
            packageNames.add(packageName);

            final ModuleIdentifier mainModuleIdentifier =
                new ModuleIdentifier(input.getFileName().toString().endsWith(".str"), false, baseName, new FSPath(input));
            final ArrayList<String> constants = new ArrayList<>(0);
            final ArrayList<STask<?>> sdfTasks = new ArrayList<>(0);
            final FSPath outputDir1 = new FSPath(outputDir);
            // Put everything together, easier for Java compilation later in the tests
            final ResourcePath str2libReplicateDir = outputDir1;
            final boolean createShadowJar = true;
            boolean supportRTree = true;
            boolean supportStr1 = true;
            final ResourcePath resolveExternals = null;
            CompileInput compileInput =
                new CompileInput(mainModuleIdentifier, projectPath, outputDir1, str2libReplicateDir,
                    packageNames, new FSPath(temporaryDirectoryPath.resolve("cacheDir")), constants,
                    strjIncludeDirs, linkedLibraries, args, sdfTasks, library, autoImportStd,
                    createShadowJar, languageIdentifier.id, str2libraries, supportRTree, supportStr1,
                    resolveExternals);
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
