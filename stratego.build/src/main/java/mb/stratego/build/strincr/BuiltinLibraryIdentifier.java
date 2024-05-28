package mb.stratego.build.strincr;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.compat.override.strc_compat.Main;

public enum BuiltinLibraryIdentifier implements IModuleImportService.ModuleIdentifier {
    StrategoLib("stratego-lib"),
    StrategoSglr("stratego-sglr"),
    StrategoGpp("stratego-gpp"),
    StrategoXtc("stratego-xtc"),
    StrategoAterm("stratego-aterm"),
    StrategoSdf("stratego-sdf"),
    Strc("strc"),
    JavaFront("java-front");

    public final String libString;
    public final String cmdArgString;

    BuiltinLibraryIdentifier(String cmdArgString) {
        this.cmdArgString = cmdArgString;
        this.libString = "lib" + cmdArgString;
    }

    @Override public boolean legacyStratego() {
        return true;
    }

    @Override public boolean isLibrary() {
        return true;
    }

    @Override public String moduleString() {
        return libString;
    }

    @Override public String toString() {
        return moduleString();
    }

    public IStrategoTerm readLibraryFile() {
        switch(this) {
            case StrategoLib:
                return Main.getLibstrategolibRtree();
            case StrategoSglr:
                return Main.getLibstrategosglrRtree();
            case StrategoGpp:
                return Main.getLibstrategogppRtree();
            case StrategoXtc:
                return Main.getLibstrategoxtcRtree();
            case StrategoAterm:
                return Main.getLibstrategoatermRtree();
            case StrategoSdf:
                return Main.getLibstrategosdfRtree();
            case Strc:
                return Main.getLibstrcRtree();
            case JavaFront:
                return Main.getLibjavafrontRtree();
        }
        throw new RuntimeException("Library was not one of the 8 built-in libraries: " + this);
    }

    public static boolean isBuiltinLibrary(String name) {
        switch(name) {
            case "stratego-lib":
            case "libstrategolib":
            case "libstratego-lib":
            case "stratego-sglr":
            case "libstratego-sglr":
            case "stratego-gpp":
            case "libstratego-gpp":
            case "stratego-xtc":
            case "libstratego-xtc":
            case "stratego-aterm":
            case "libstratego-aterm":
            case "stratego-sdf":
            case "libstratego-sdf":
            case "strc":
            case "libstrc":
            case "java-front":
            case "libjava-front":
                return true;
            default:
                return false;
        }
    }

    public static @Nullable BuiltinLibraryIdentifier fromString(String name) {
        switch(name) {
            case "stratego-lib":
            case "libstrategolib":
            case "libstratego-lib": {
                return StrategoLib;
            }
            case "stratego-sglr":
            case "libstratego-sglr": {
                return StrategoSglr;
            }
            case "stratego-gpp":
            case "libstratego-gpp": {
                return StrategoGpp;
            }
            case "stratego-xtc":
            case "libstratego-xtc": {
                return StrategoXtc;
            }
            case "stratego-aterm":
            case "libstratego-aterm": {
                return StrategoAterm;
            }
            case "stratego-sdf":
            case "libstratego-sdf": {
                return StrategoSdf;
            }
            case "strc":
            case "libstrc": {
                return Strc;
            }
            case "java-front":
            case "libjava-front": {
                return JavaFront;
            }
            default:
                return null;
        }
    }
}
