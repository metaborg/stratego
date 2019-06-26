package mb.stratego.build;

import mb.pie.api.ExecException;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.resource.IResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.strategoxt.lang.compat.override.strc_compat.Main;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public interface Library extends Serializable {
    IStrategoTerm readLibraryFile(ITermFactory factory) throws ExecException, IOException;

    @Nullable File fileToRead() throws MalformedURLException;

    static Library fromString(IResourceService resourceService, String name) throws FileSystemException {
        final @Nullable Builtin builtinLibrary = Builtin.fromString(name);
        if(builtinLibrary != null) {
            return builtinLibrary;
        }
        return new RTree(resourceService.resolve(name).getURL().toString());
    }

    static String normalizeBuiltin(String name) {
        final @Nullable Builtin b = Builtin.fromString(name);
        if(b == null) {
            return name;
        } else {
            return b.libString;
        }
    }

    enum Builtin implements Library {
        // @formatter:off
        StrategoLib("stratego-lib"),
        StrategoSglr("stratego-sglr"),
        StrategoGpp("stratego-gpp"),
        StrategoXtc("stratego-xtc"),
        StrategoAterm("stratego-aterm"),
        StrategoSdf("stratego-sdf"),
        Strc("strc"),
        JavaFront("java-front");
        // @formatter:on

        public final String libString;
        public final String cmdArgString;

        Builtin(String cmdArgString) {
            this.cmdArgString = cmdArgString;
            this.libString = "lib" + cmdArgString;
        }

        @Override public @Nullable File fileToRead() {
            return null;
        }

        @Override public IStrategoTerm readLibraryFile(ITermFactory factory) throws ExecException {
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
            throw new ExecException("Library was not one of the 8 built-in libraries: " + this);
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

        public static @Nullable Builtin fromString(String name) {
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

    class RTree implements Library {
        private final String pathURLString;

        RTree(String pathURLString) {
            this.pathURLString = pathURLString;
        }

        @Override public @Nullable File fileToRead() throws MalformedURLException {
            URL url = new URL(pathURLString);
            if(url.getProtocol().equals("jar")) {
                url = new URL(url.getPath());
            }
            if(url.getProtocol().equals("file")) {
                return new File(url.getPath().split("!", 2)[0].split("/", 2)[1]);
            }
            /* This will probably fail with an exception, but that's fine because we don't know how to handle
             *  non-jar/file protocols.
             */
            return new File(url.toString());
        }

        @Override public IStrategoTerm readLibraryFile(ITermFactory factory) throws ExecException, IOException {
            return new TermReader(factory).parseFromStream(new URL(pathURLString).openStream());
        }
    }
}
