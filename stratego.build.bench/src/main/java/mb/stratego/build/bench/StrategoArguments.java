package mb.stratego.build.bench;

import mb.stratego.build.Library;

import org.metaborg.util.cmd.Arguments;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "unused" }) class StrategoArguments {
    private final String[] original;
    final Arguments extraArguments = new Arguments();

    final String inputFile;
    final String outputFile;
    private final @Nullable String mainStrategy;
    private final boolean clean;
    final List<String> includeDirs;
    private final int stacktracing;
    final Map<String, String> constants;
    private final boolean shareConstructors;
    private final int optimizationLevel;
    final @Nullable String javaPackageName;
    final EnumSet<Library.Builtin> builtinLibraries;
    private final List<String> otherLibraries;
    private final boolean isLibrary;
    private final boolean produceAst;
    private final boolean produceCore;
    private final boolean singleStrategy;
    private final boolean boilerplate;
    private final boolean preferStr;
    private final @Nullable String defaultSyntax;
    final @Nullable String cacheDir;
    private final boolean fusion;
    private final boolean asFix;
    private final boolean xSepCompTool;
    private final int verbose;
    private final int keep;
    private final int statistics;
    final boolean showHelp;
    final boolean showVersion;
    private final EnumSet<StrategoWarningCategory> showWarnings;

    private StrategoArguments(String[] args) {
        this.original = args;

        @Nullable String inputFile = null;
        @Nullable String outputFile = null;
        @Nullable String mainStrategy = null;
        boolean clean = false;
        List<String> includeDirs = new ArrayList<>();
        int stacktracing = 1;
        Map<String, String> constants = new HashMap<>();
        boolean shareConstructors = true;
        int optimizationLevel = 0;
        @Nullable String javaPackageName = null;
        EnumSet<Library.Builtin> builtinLibraries = EnumSet.noneOf(Library.Builtin.class);
        List<String> otherLibraries = new ArrayList<>();
        boolean isLibrary = false;
        boolean produceAst = false;
        boolean produceCore = false;
        boolean singleStrategy = false;
        boolean boilerplate = false;
        boolean preferStr = false;
        @Nullable String defaultSyntax = null;
        @Nullable String cacheDir = null;
        boolean fusion = true;
        boolean asFix = false;
        boolean xSepCompTool = false;
        int verbose = 1;
        int keep = 0;
        int statistics = 0;
        boolean showHelp = false;
        boolean showVersion = false;

        @Nullable EnumSet<StrategoWarningCategory> showWarnings = null;
        for(int i = 0, argsLength = args.length; i < argsLength; i++) {
            String arg = args[i];
            switch(arg) {
                case "--input":
                case "-i":
                    i++;
                    inputFile = args[i];
                    break;
                case "--output":
                case "-o":
                    i++;
                    outputFile = args[i];
                    break;
                case "--main":
                case "-m":
                    i++;
                    mainStrategy = args[i];
                    this.extraArguments.add("-m", args[i]);
                    break;
                case "--clean":
                    clean = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "-I":
                case "--Include":
                    i++;
                    includeDirs.add(args[i]);
                    break;
                case "stacktrace":
                case "-s":
                    i++;
                    stacktracing = Integer.parseInt(args[i]);
                    this.extraArguments.add("-s", args[i]);
                    break;
                case "-D":
                    i++;
                    String[] nv = args[i].split("=", 2);
                    assert nv.length == 2;
                    constants.put(nv[0], nv[1]);
                    break;
                case "-sc":
                    i++;
                    if(Objects.equals(args[i], "off")) {
                        shareConstructors = false;
                    }
                    this.extraArguments.add("-sc", args[i]);
                    break;
                case "-O":
                    i++;
                    optimizationLevel = Integer.parseInt(args[i]);
                    this.extraArguments.add("-O", args[i]);
                    break;
                case "-p":
                    i++;
                    javaPackageName = args[i];
                    break;
                case "-la":
                    i++;
                    Library.Builtin lib = Library.Builtin.fromString(args[i]);
                    if(lib != null) {
                        builtinLibraries.add(lib);
                    } else {
                        otherLibraries.add(args[i]);
                        this.extraArguments.add("-la", args[i]);
                    }
                    break;
                case "--library":
                    isLibrary = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "--ast":
                    produceAst = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "-F":
                    produceCore = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "--single-strategy":
                    singleStrategy = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "--boilerplate":
                    boilerplate = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "--prefer-str":
                    preferStr = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "--default-syntax":
                    i++;
                    defaultSyntax = args[i];
                    this.extraArguments.add("--default-syntax", args[i]);
                    break;
                case "--cache-dir":
                    i++;
                    cacheDir = args[i];
                    this.extraArguments.add("--cache-dir", args[i]);
                    break;
                case "--fusion":
                    fusion = false;
                    this.extraArguments.add(args[i]);
                    break;
                case "--asfix":
                    asFix = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "--Xsep-comp-tool":
                    xSepCompTool = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "-S":
                case "--silent":
                    verbose = 0;
                    this.extraArguments.add(args[i]);
                    break;
                case "--verbose":
                    i++;
                    verbose = Integer.parseInt(args[i]);
                    this.extraArguments.add("--verbose", args[i]);
                    break;
                case "-k":
                case "--keep":
                    i++;
                    keep = Integer.parseInt(args[i]);
                    this.extraArguments.add("--keep", args[i]);
                    break;
                case "--statistics":
                    i++;
                    statistics = Integer.parseInt(args[i]);
                    this.extraArguments.add("--statistics", args[i]);
                    break;
                case "-h":
                case "-?":
                case "--help":
                    showHelp = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "-v":
                case "--version":
                case "--about":
                    showVersion = true;
                    this.extraArguments.add(args[i]);
                    break;
                case "-W":
                case "--warning":
                    if(showWarnings == null) {
                        showWarnings = EnumSet.noneOf(StrategoWarningCategory.class);
                    }
                    i++;
                    switch(args[i]) {
                        case "all":
                            showWarnings = EnumSet.allOf(StrategoWarningCategory.class);
                            break;
                        case "no-C":
                            showWarnings.add(StrategoWarningCategory.NoC);
                            break;
                        case "debug-arguments":
                            showWarnings.add(StrategoWarningCategory.DebugArguments);
                            break;
                        case "obsolete-strategy-calls":
                            showWarnings.add(StrategoWarningCategory.ObsoleteStrategyCalls);
                            break;
                        case "maybe-unbound-variables":
                            showWarnings.add(StrategoWarningCategory.MaybeUnboundVariables);
                            break;
                        case "lower-case-constructors":
                            showWarnings.add(StrategoWarningCategory.LowerCaseConstructors);
                            break;
                    }
                    this.extraArguments.add("-W", args[i]);
            }
        }
        assert inputFile != null;
        this.inputFile = inputFile;
        assert outputFile != null;
        this.outputFile = outputFile;
        this.mainStrategy = mainStrategy;
        this.clean = clean;
        this.includeDirs = includeDirs;
        this.stacktracing = stacktracing;
        this.constants = constants;
        this.shareConstructors = shareConstructors;
        this.optimizationLevel = optimizationLevel;
        this.javaPackageName = javaPackageName;
        this.builtinLibraries = builtinLibraries;
        this.otherLibraries = otherLibraries;
        this.isLibrary = isLibrary;
        this.produceAst = produceAst;
        this.produceCore = produceCore;
        this.singleStrategy = singleStrategy;
        this.boilerplate = boilerplate;
        this.preferStr = preferStr;
        this.defaultSyntax = defaultSyntax;
        this.cacheDir = cacheDir;
        this.fusion = fusion;
        this.asFix = asFix;
        this.xSepCompTool = xSepCompTool;
        this.verbose = verbose;
        this.keep = keep;
        this.statistics = statistics;
        this.showHelp = showHelp;
        this.showVersion = showVersion;
        this.showWarnings = showWarnings != null ? showWarnings : EnumSet
            .of(StrategoWarningCategory.DebugArguments, StrategoWarningCategory.ObsoleteStrategyCalls,
                StrategoWarningCategory.LowerCaseConstructors);
    }

    static StrategoArguments fromArgs(String[] args) {
        return new StrategoArguments(args);
    }
}
