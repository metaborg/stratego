import kotlin.Unit;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.ResourceKey;
import mb.pie.api.TaskKey;
import mb.pie.api.exec.BottomUpExecutor;
import mb.pie.api.fs.ResourceKt;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.stratego.build.StrIncr;
import mb.stratego.build.StrIncrModule;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.util.cmd.Arguments;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        Spoofax spoofax = new Spoofax(new SpoofaxModuleExtension(), new StrIncrModule(), new GuiceTaskDefsModule());

        spoofax.languageDiscoveryService.languageFromArchive(
            spoofax.resourceService.resolve(Main.class.getResource("/stratego.spoofax-language").toURI()));

        GuiceTaskDefs guiceTaskDefs = spoofax.injector.getInstance(GuiceTaskDefs.class);

        // We need to create the PIE runtime, using a PieBuilderImpl.
        final PieBuilder pieBuilder = new PieBuilderImpl();

        // file system store
        //        LMDBStoreKt.withLMDBStore(pieBuilder, new File("/tmp/lmdb"));

        pieBuilder.withTaskDefs(guiceTaskDefs);
        // For example purposes, we use verbose logging which will output to stdout.
        pieBuilder.withLogger(StreamLogger.verbose());
        final Pie pie = pieBuilder.build();

        // We always need to do a topDown build first as a clean build.
        /* This is the strj command for non-incremental build from which we derive the StrIncr.Input arguments:
        strj
         -i /Users/jeff/Git/spoofax_dev_guest/incremental/trans/incremental.str
         -o /Users/jeff/Git/spoofax_dev_guest/incremental/src-gen/stratego-java/incremental/trans/Main.java
         -p incremental.trans
         --library
         --clean
         -I /Users/jeff/Git/spoofax_dev_guest/incremental/src-gen
         -I /Users/jeff/Git/spoofax_dev_guest/incremental/trans
         -I /Users/jeff/Git/spoofax_dev_guest/incremental
         -I /Users/jeff/Git/spoofax_dev_guest/incremental/src-gen/nabl2/collection
         -I /Users/jeff/Git/spoofax_dev_guest/incremental/target/replicate/strj-includes
         --cache-dir /Users/jeff/Git/spoofax_dev_guest/incremental/target/stratego-cache
         -la stratego-lib
         -la stratego-sglr
         -la stratego-gpp
         -la stratego-xtc
         -la stratego-aterm
         -la stratego-sdf
         -la strc
         -la java-front
         -la incremental.strategies
        */
        Path projectLocation = Paths.get(Main.class.getResource("/languageProject").toURI());

        URI inputFile = projectLocation.resolve("trans/incremental.str").toUri();

        File outputFile = projectLocation.resolve("src-gen/stratego-java/incremental/trans/Main.java").toFile();

        String javaPackageName = "incremental.trans";

        // @formatter:off
        List<File> includeDirs = Arrays.asList(
            projectLocation.resolve("src-gen").toFile(),
            projectLocation.resolve("trans").toFile(),
            projectLocation.toFile(),
            projectLocation.resolve("src-gen/nabl2/collection").toFile(),
            projectLocation.resolve("target/replicate/strj-includes").toFile()
        );
        // @formatter:on

        File cacheDir = projectLocation.resolve("target/stratego-cache").toFile();

        // @formatter:off
        List<String> builtinLibs = Arrays.asList(
            "stratego-lib",
            "stratego-sglr",
            "stratego-gpp",
            "stratego-xtc",
            "stratego-aterm",
            "stratego-sdf",
            "strc",
            "java-front"
        );
        // @formatter:on

        Arguments extraArgs = new Arguments();
        extraArgs.add("-la", "incremental.strategies");

        StrIncr strIncr = spoofax.injector.getInstance(StrIncr.class);
        StrIncr.Input strIncrInput =
            new StrIncr.Input(inputFile.toURL(), javaPackageName, includeDirs, builtinLibs, cacheDir, extraArgs,
                outputFile, Collections.emptyList(), projectLocation.toFile());
        pie.getTopDownExecutor().newSession().requireInitial(strIncr.createTask(strIncrInput));

        // We can do a bottom up build with a changeset
        System.out.println("\n\n\nEmpty Change Set BottomUp\n\n\n");
        Set<ResourceKey> changedResources = new HashSet<>();
        BottomUpExecutor bottomUpExecutor = pie.getBottomUpExecutor();
        bottomUpExecutor.setObserver(new TaskKey(strIncr.getId(), strIncr.key(strIncrInput)), s -> {
            // Use jmh blackhole here to make sure nothing is optimized away
            return Unit.INSTANCE;
        });
        bottomUpExecutor.requireBottomUp(changedResources);

        System.out.println("\n\n\nMain File Change Set BottomUp (No change in file)\n\n\n");
        changedResources.add(ResourceKt.toResourceKey(new File(inputFile)));
        bottomUpExecutor.requireBottomUp(changedResources);

        pie.close();
    }
}
