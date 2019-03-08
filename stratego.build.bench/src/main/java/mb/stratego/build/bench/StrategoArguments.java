package mb.stratego.build.bench;

import mb.stratego.build.StrIncrFrontLib;

import org.metaborg.util.cmd.Arguments;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class StrategoArguments {
    final String[] original;
    final Arguments extraArguments = new Arguments();

    String inputFile = null;
    String outputFile = null;
    private String mainStrategy = null;
    private boolean clean = false;
    List<String> includeDirs = new ArrayList<>();
    private int stacktracing = 1;
    private Map<String, String> constants = new HashMap<>();
    private boolean shareConstructors = true;
    private int optimizationLevel = 0;
    String javaPackageName = null;
    EnumSet<StrIncrFrontLib.BuiltinLibrary> builtinLibraries = EnumSet.noneOf(StrIncrFrontLib.BuiltinLibrary.class);
    private List<String> otherLibraries = new ArrayList<>();
    private boolean isLibrary = false;
    private boolean produceAst = false;
    private boolean produceCore = false;
    private boolean singleStrategy = false;
    private boolean boilerplate = false;
    private boolean preferStr = false;
    private String defaultSyntax = null;
    String cacheDir = null;
    private boolean fusion = true;
    private boolean asFix = false;
    private boolean xSepCompTool = false;
    private int verbose = 1;
    private int keep = 0;
    private int statistics = 0;
    boolean showHelp = false;
    boolean showVersion = false;
    private EnumSet<StrategoWarningCategory> showWarnings = EnumSet.of(StrategoWarningCategory.DebugArguments, StrategoWarningCategory.ObsoleteStrategyCalls, StrategoWarningCategory.LowerCaseConstructors);

    private StrategoArguments(String[] args) {
        this.original = args;
    }

    static StrategoArguments fromArgs(String[] args) {
        StrategoArguments result = new StrategoArguments(args);
        EnumSet<StrategoWarningCategory> showWarnings = null;
        for(int i = 0, argsLength = args.length; i < argsLength; i++) {
            String arg = args[i];
            switch(arg) {
                case "--input":
                case "-i":
                    i++;
                    result.inputFile = args[i];
                    break;
                case "--output":
                case "-o":
                    i++;
                    result.outputFile = args[i];
                    break;
                case "--main":
                case "-m":
                    i++;
                    result.mainStrategy = args[i];
                    result.extraArguments.add("-m", args[i]);
                    break;
                case "--clean":
                    result.clean = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "-I":
                case "--Include":
                    i++;
                    result.includeDirs.add(args[i]);
                    break;
                case "stacktrace":
                case "-s":
                    i++;
                    result.stacktracing = Integer.parseInt(args[i]);
                    result.extraArguments.add("-s", args[i]);
                    break;
                case "-D":
                    i++;
                    String[] nv = args[i].split("=", 2);
                    assert nv.length == 2;
                    result.constants.put(nv[0], nv[1]);
                    result.extraArguments.add("-D", args[i]);
                    break;
                case "-sc":
                    i++;
                    if(Objects.equals(args[i], "off")) {
                        result.shareConstructors = false;
                    }
                    result.extraArguments.add("-sc", args[i]);
                    break;
                case "-O":
                    i++;
                    result.optimizationLevel = Integer.parseInt(args[i]);
                    result.extraArguments.add("-O", args[i]);
                    break;
                case "-p":
                    i++;
                    result.javaPackageName = args[i];
                    break;
                case "-la":
                    i++;
                    StrIncrFrontLib.BuiltinLibrary lib = StrIncrFrontLib.BuiltinLibrary.fromString(args[i]);
                    if(lib != null) {
                        result.builtinLibraries.add(lib);
                    } else {
                        result.otherLibraries.add(args[i]);
                        result.extraArguments.add("-la", args[i]);
                    }
                    break;
                case "--library":
                    result.isLibrary = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "--ast":
                    result.produceAst = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "-F":
                    result.produceCore = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "--single-strategy":
                    result.singleStrategy = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "--boilerplate":
                    result.boilerplate = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "--prefer-str":
                    result.preferStr = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "--default-syntax":
                    i++;
                    result.defaultSyntax = args[i];
                    result.extraArguments.add("--default-syntax", args[i]);
                    break;
                case "--cache-dir":
                    i++;
                    result.cacheDir = args[i];
                    result.extraArguments.add("--cache-dir", args[i]);
                    break;
                case "--fusion":
                    result.fusion = false;
                    result.extraArguments.add(args[i]);
                    break;
                case "--asfix":
                    result.asFix = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "--Xsep-comp-tool":
                    result.xSepCompTool = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "-S":
                case "--silent":
                    result.verbose = 0;
                    result.extraArguments.add(args[i]);
                    break;
                case "--verbose":
                    i++;
                    result.verbose = Integer.parseInt(args[i]);
                    result.extraArguments.add("--verbose", args[i]);
                    break;
                case "-k":
                case "--keep":
                    i++;
                    result.keep = Integer.parseInt(args[i]);
                    result.extraArguments.add("--keep", args[i]);
                    break;
                case "--statistics":
                    i++;
                    result.statistics = Integer.parseInt(args[i]);
                    result.extraArguments.add("--statistics", args[i]);
                    break;
                case "-h":
                case "-?":
                case "--help":
                    result.showHelp = true;
                    result.extraArguments.add(args[i]);
                    break;
                case "-v":
                case "--version":
                case "--about":
                    result.showVersion = true;
                    result.extraArguments.add(args[i]);
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
                    result.extraArguments.add("-W", args[i]);
            }
            if(showWarnings != null) {
                result.showWarnings = showWarnings;
            }
        }
        return result;
    }
}
