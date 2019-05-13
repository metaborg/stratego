package mb.stratego.build.bench;

import mb.pie.api.None;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.PieSession;
import mb.pie.api.Task;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.store.lmdb.LMDBStore;
import mb.pie.taskdefs.guice.GuiceTaskDefs;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.resource.ResourceKey;
import mb.resource.fs.FSPath;
import mb.stratego.build.Library;
import mb.stratego.build.StrIncr;
import mb.stratego.build.StrIncrModule;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.core.resource.ResourceUtils;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.resource.FileSelectorUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            run();
        } else {
            run(StrategoArguments.fromArgs(args));
        }
    }

    private static void run(StrategoArguments strategoArguments) throws Exception {
        if(strategoArguments.showHelp) {
            // @formatter:off
            System.out.println("\n"
                + "Options:\n"
                + "   -i f|--input f   Read input from f\n"
                + "   -o f|--output f  Write output to f\n"
                + "   --main f | -m f    Main strategy to compile (default: main)\n"
                + "   --clean            Remove all existing Java files in the output directory\n"
                + "   -I d | --Include d Include modules from directory d\n"
                + "   --stacktrace i | -s i  Enable stacktracing (0 = no, 1 = always [default], 2 = only if assertions"
                    + " (java -ea) enabled for a class)\n"
                + "   -D name=value      Define a constant value strategy\n"
                + "   -sc <on|off>       Assume all term constructors are shared (default: on)\n"
                + "   -O n               Optimization level (0 = no optimization)\n"
                + "\n"
                + "   Library options:\n"
                + "\n"
                + "   -p <name>          Set package name <name> (should be unique for each library)\n"
                + "   -la <name>         Include library in package <name>\n"
                + "   --library | --lib  Build a library instead of an application\n" + "\n"
                + "   Configuration of the Stratego compiler:\n"
                + "\n"
                + "   --ast              Produce abstract syntax tree of packed program\n"
                + "   -F                 Produce core after front-end\n"
                + "   --single-strategy    Generate from a single strategy definition\n"
                + "   --boilerplate        Generate boilerplate (main/interopregister)\n"
                + "   --prefer-str       Prefer .str files over .rtree files\n"
                + "   --default-syntax syn        use syntax syn as default\n"
                + "   --cache-dir <dir>  Maintain a compilation cache in <dir>\n"
                + "   --fusion           Toggle specialize applications of innermost (default: on)\n"
                + "   --asfix            Concrete syntax parts are not imploded\n"
                + "   --Xsep-comp-tool  Compile based on sep-comp-tool (experimental)\n" + "\n"
                + "   General options:\n"
                + "\n"
                + "   -S|--silent      Silent execution (same as --verbose 0)\n"
                + "   --verbose i      Verbosity level i (default 1)\n"
                + "                    ( i as a number or as a verbosity descriptor:\n"
                + "                      emergency, alert, critical, error,\n"
                + "                      warning, notice, info, debug, vomit )\n"
                + "   -k i | --keep i  Keep intermediates (default 0)\n"
                + "   --statistics i   Print statistics (default 0 = none)\n"
                + "   -h | --help        Show help\n"
                + "   -v | --version     Display program's version\n"
                + "   -W,--warning C   Report warnings falling in category C. Categories:\n"
                + "                      all                      all categories\n"
                + "                      no-C                     no warnings in category C\n"
                + "                      debug-arguments          missing build operator [ on ]\n"
                + "                      obsolete-strategy-calls  obsolete strategies [ on ]\n"
                + "                      maybe-unbound-variables  unbound variables [ off ]\n"
                + "                      lower-case-constructors  lower-case constructors [ on ]\n" + "\n"
                + "   -h|-?|--help     Display usage information\n"
                + "   --about          Display information about this program\n"
                + "   --version        Same as --about\n"
                + "\n" + "Description:");
            // @formatter:on
            System.exit(0);
        } else if(strategoArguments.showVersion) {
            System.out.println("STRJ ${version}\n");
            System.exit(0);
        }

        Spoofax spoofax =
            new Spoofax(new NullEditorSingleFileProject(), new StrIncrModule(), new GuiceTaskDefsModule());

        spoofax.languageDiscoveryService
            .languageFromDirectory(spoofax.resourceService.resolve(Main.class.getResource("/stratego.lang/").toURI()));

        GuiceTaskDefs guiceTaskDefs = spoofax.injector.getInstance(GuiceTaskDefs.class);

        // We need to create the PIE runtime, using a PieBuilderImpl.
        final PieBuilder pieBuilder = new PieBuilderImpl();

        // file system store
        LMDBStore.withLMDBStore(pieBuilder, new File("/tmp/lmdb"));

        pieBuilder.withTaskDefs(guiceTaskDefs);
        // For example purposes, we use verbose logging which will output to stdout.
        pieBuilder.withLogger(StreamLogger.verbose());
        // We always need to do a topDown build first as a clean build.

        File inputFile = Paths.get(strategoArguments.inputFile).toFile();

        List<File> includeDirs = new ArrayList<>(strategoArguments.includeDirs.size());
        for(String includeDir : strategoArguments.includeDirs) {
            final File include = Paths.get(includeDir).toFile();
            includeDirs.add(include);
            discoverDialects(spoofax, include.getAbsolutePath());
        }

        spoofax.languageDiscoveryService
            .languageFromDirectory(spoofax.resourceService.resolve(Main.class.getResource("/stratego.lang/").toURI()));

        List<String> builtinLibs = new ArrayList<>(strategoArguments.builtinLibraries.size());
        for(Library.Builtin builtinLibrary : strategoArguments.builtinLibraries) {
            builtinLibs.add(builtinLibrary.cmdArgString);
        }

        StrIncr strIncr = spoofax.injector.getInstance(StrIncr.class);

        final Path projectLocation =
            Paths.get(strategoArguments.inputFile).toAbsolutePath().normalize().getParent().getParent();

        final List<String> constants = new ArrayList<>(strategoArguments.constants.size());
        for(Map.Entry<String, String> e : strategoArguments.constants.entrySet()) {
            constants.add(e.getKey() + '=' + e.getValue());
        }
        try(final Pie pie = pieBuilder.build()) {
            StrIncr.Input strIncrInput =
                new StrIncr.Input(inputFile, strategoArguments.javaPackageName, includeDirs, builtinLibs,
                    strategoArguments.cacheDir == null ? null : Paths.get(strategoArguments.cacheDir).toFile(),
                    constants, strategoArguments.extraArguments, Paths.get(strategoArguments.outputFile).toFile(),
                    Collections.emptyList(), projectLocation.toFile());
            try(final PieSession session = pie.newSession()) {
                session.requireTopDown(strIncr.createTask(strIncrInput));
            }
        }
    }

    private static void discoverDialects(Spoofax spoofax, String projectLocation) throws FileSystemException {
        final FileObject location = spoofax.resourceService.resolve(projectLocation);
        spoofax.dialectProcessor.update(location, ResourceUtils
            .toChanges(ResourceUtils.find(location, new SpecialIgnoresSelector()), ResourceChangeKind.Create));
    }

    private static void run() throws Exception {
        Spoofax spoofax =
            new Spoofax(new NullEditorConfigBasedProject(), new StrIncrModule(), new GuiceTaskDefsModule());

        spoofax.languageDiscoveryService
            .languageFromDirectory(spoofax.resourceService.resolve(Main.class.getResource("/stratego.lang/").toURI()));

        GuiceTaskDefs guiceTaskDefs = spoofax.injector.getInstance(GuiceTaskDefs.class);

        // We need to create the PIE runtime, using a PieBuilderImpl.
        final PieBuilder pieBuilder = new PieBuilderImpl();

        // file system store
        //        LMDBStore.withLMDBStore(pieBuilder, new File("/tmp/lmdb"));

        pieBuilder.withTaskDefs(guiceTaskDefs);
        // For example purposes, we use verbose logging which will output to stdout.
        pieBuilder.withLogger(StreamLogger.verbose());
        //        // N.B. extremely slow but maybe useful for debugging the failures.
        //        pieBuilder.withLayer((taskDefs, logger) -> {
        //            final ValidationLayer.ValidationOptions options = new ValidationLayer.ValidationOptions();
        //            options.checkKeyObjects = true;
        //            options.checkInputObjects = true;
        //            options.checkOutputObjects = true;
        //            options.throwWarnings = true;
        //            return new ValidationLayer(options, taskDefs, logger);
        //        });

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
        spoofax.resourceService.resolve("/tmp/stratego.build.bench/")
            .copyFrom(spoofax.resourceService.resolve(Main.class.getResource("/languageProject").toURI()),
                FileSelectorUtils.all());
        Path projectLocation = Paths.get("/tmp/stratego.build.bench/");

        File inputFile = projectLocation.resolve("trans/incremental.str").toFile();

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
            new StrIncr.Input(inputFile, javaPackageName, includeDirs, builtinLibs, cacheDir, Collections.emptyList(),
                extraArgs, outputFile, Collections.emptyList(), projectLocation.toFile());
        final Task<None> compileTask = strIncr.createTask(strIncrInput);
        try(final Pie pie = pieBuilder.build()) {
            try(final PieSession session = pie.newSession()) {
                session.requireTopDown(compileTask);
            }

            // We can do a bottom up build with a changeset
            System.out.println("\n\n\nEmpty Change Set BottomUp\n\n\n");
            Set<ResourceKey> changedResources = new HashSet<>();
            pie.setObserver(compileTask, s -> {
                // FIXME: Use jmh blackhole here to make sure nothing is optimized away
            });
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(changedResources);
            }

            System.out.println("\n\n\nMain File Change Set BottomUp (No change in file)\n\n\n");
            changedResources.add(new FSPath(inputFile));
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(changedResources);
            }
        }
    }
}
