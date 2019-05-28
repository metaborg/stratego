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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    @SuppressWarnings("WeakerAccess") public static final String TMPDIR = System.getProperty("java.io.tmpdir");

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            runLanguageProject();
        } else if(args.length == 1 && args[0].equals("languageProject")) {
            runLanguageProject();
        } else if(args.length == 1 && args[0].equals("tiger")) {
            runTiger();
        } else {
            runLanguageProject(StrategoArguments.fromArgs(args));
        }
    }

    private static void runLanguageProject(StrategoArguments strategoArguments) throws Exception {
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
        LMDBStore.withLMDBStore(pieBuilder, Paths.get(TMPDIR, "lmdb").toFile());

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

    private static void runLanguageProject() throws Exception {
        Spoofax spoofax =
            new Spoofax(new NullEditorConfigBasedProject(), new StrIncrModule(), new GuiceTaskDefsModule());

        spoofax.languageDiscoveryService
            .languageFromDirectory(spoofax.resourceService.resolve(Main.class.getResource("/stratego.lang/").toURI()));

        GuiceTaskDefs guiceTaskDefs = spoofax.injector.getInstance(GuiceTaskDefs.class);

        // We need to create the PIE runtime, using a PieBuilderImpl.
        final PieBuilder pieBuilder = new PieBuilderImpl();

        // file system store
        //        LMDBStore.withLMDBStore(pieBuilder, Paths.get(TMPDIR, "lmdb").toFile());

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
        Path projectLocation = Paths.get(TMPDIR, "stratego.build.bench");
        spoofax.resourceService.resolve(projectLocation.toString())
            .copyFrom(spoofax.resourceService.resolve(Main.class.getResource("/languageProject").toURI()),
                FileSelectorUtils.all());

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
            long startTime = System.nanoTime();
            try(final PieSession session = pie.newSession()) {
                session.requireTopDown(compileTask);
            }
            long buildTime = System.nanoTime();
            System.out.println("\"First run took\", " + (buildTime - startTime));
            startTime = buildTime;

            // We can do a bottom up build with a changeset
            Set<ResourceKey> changedResources = new HashSet<>();
            pie.setObserver(compileTask, s -> {
                // FIXME: Use jmh blackhole here to make sure nothing is optimized away
            });
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(changedResources);
            }
            buildTime = System.nanoTime();
            System.out.println("\"Empty change set bottomup took\", " + (buildTime - startTime));
            startTime = buildTime;

            changedResources.add(new FSPath(inputFile));
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(changedResources);
            }
            buildTime = System.nanoTime();
            System.out.println("\"Main file touched bottomup took\", " + (buildTime - startTime));

            changedResources = new HashSet<>();
            for(Path path : Files.newDirectoryStream(projectLocation.resolve("trans"), "*.str")) {
                changedResources.add(new FSPath(path));
            }
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(changedResources);
            }
            buildTime = System.nanoTime();
            System.out.println("\"All source files touched bottomup took\", " + (buildTime - startTime));
        }
    }

    private static void runTiger() throws Exception {
        Spoofax spoofax =
            new Spoofax(new NullEditorConfigBasedProject(), new StrIncrModule(), new GuiceTaskDefsModule());

        spoofax.languageDiscoveryService
            .languageFromDirectory(spoofax.resourceService.resolve(Main.class.getResource("/stratego.lang/").toURI()));

        GuiceTaskDefs guiceTaskDefs = spoofax.injector.getInstance(GuiceTaskDefs.class);

        // We need to create the PIE runtime, using a PieBuilderImpl.
        final PieBuilder pieBuilder = new PieBuilderImpl();

        // file system store
        //        LMDBStore.withLMDBStore(pieBuilder, Paths.get(TMPDIR, "lmdb").toFile());

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
        -i /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/trans/tiger.str
        -o /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/src-gen/stratego-java/org/metaborg/lang/tiger/trans/Main.java
        -p org.metaborg.lang.tiger.trans
        --library
        --clean
        -I /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/src-gen
        -I /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/trans
        -I /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger
        -I /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/src-gen/nabl2/collection
        -I /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/src-gen/nabl2/dynsem
        -I /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/target/replicate/strj-includes
        --cache-dir /Users/jeff/Git/metaborg-tiger/org.metaborg.lang.tiger/target/stratego-cache
        -la stratego-lib
        -la stratego-sglr
        -la stratego-gpp
        -la stratego-xtc
        -la stratego-aterm
        -la stratego-sdf
        -la strc
        -la java-front
        -la org.metaborg.lang.tiger.strategies
        */
        Path projectLocation = Paths.get(TMPDIR, "stratego.build.bench");
        spoofax.resourceService.resolve(projectLocation.toString())
            .copyFrom(spoofax.resourceService.resolve(Main.class.getResource("/tiger").toURI()),
                FileSelectorUtils.all());

        File inputFile = projectLocation.resolve("trans/tiger.str").toFile();

        File outputFile = projectLocation.resolve("src-gen/stratego-java/org/metaborg/lang/tiger/trans/Main.java").toFile();

        String javaPackageName = "org.metaborg.lang.tiger.trans";

        // @formatter:off
        List<File> includeDirs = Arrays.asList(
            projectLocation.resolve("src-gen").toFile(),
            projectLocation.resolve("trans").toFile(),
            projectLocation.toFile(),
            projectLocation.resolve("src-gen/nabl2/collection").toFile(),
            projectLocation.resolve("src-gen/nabl2/dynsem").toFile(),
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
        extraArgs.add("-la", "org.metaborg.lang.tiger.strategies");

        StrIncr strIncr = spoofax.injector.getInstance(StrIncr.class);
        StrIncr.Input strIncrInput =
            new StrIncr.Input(inputFile, javaPackageName, includeDirs, builtinLibs, cacheDir, Collections.emptyList(),
                extraArgs, outputFile, Collections.emptyList(), projectLocation.toFile());
        final Task<None> compileTask = strIncr.createTask(strIncrInput);
        try(final Pie pie = pieBuilder.build()) {
            long startTime = System.nanoTime();
            try(final PieSession session = pie.newSession()) {
                session.requireTopDown(compileTask);
            }
            long buildTime = System.nanoTime();
            System.out.println("\"First run took\", " + (buildTime - startTime));
            startTime = buildTime;

            // We can do a bottom up build with a changeset
            Set<ResourceKey> changedResources = new HashSet<>();
            pie.setObserver(compileTask, s -> {
                // FIXME: Use jmh blackhole here to make sure nothing is optimized away
            });
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(changedResources);
            }
            buildTime = System.nanoTime();
            System.out.println("\"Empty change set bottomup took\", " + (buildTime - startTime));
            startTime = buildTime;

            changedResources.add(new FSPath(inputFile));
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(changedResources);
            }
            buildTime = System.nanoTime();
            System.out.println("\"Main file touched bottomup took\", " + (buildTime - startTime));

            final Set<ResourceKey> sourceChangedResources = new HashSet<>();
            Files.walkFileTree(projectLocation.resolve("trans"), new SimpleFileVisitor<Path>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(file.endsWith(".str")) {
                        sourceChangedResources.add(new FSPath(file));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(sourceChangedResources);
            }
            buildTime = System.nanoTime();
            System.out.println("\"All source files touched bottomup took\", " + (buildTime - startTime));

            Files.walkFileTree(projectLocation.resolve("src-gen"), new SimpleFileVisitor<Path>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(file.endsWith(".str")) {
                        sourceChangedResources.add(new FSPath(file));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            try(final PieSession session = pie.newSession()) {
                session.requireBottomUp(sourceChangedResources);
            }
            buildTime = System.nanoTime();
            System.out.println("\"All source/src-gen files touched bottomup took\", " + (buildTime - startTime));
        }
    }
}
