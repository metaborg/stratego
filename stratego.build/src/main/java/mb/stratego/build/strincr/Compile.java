package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Function;
import mb.pie.api.TaskDef;
import mb.resource.Resource;

public class Compile implements TaskDef<Compile.Input, Compile.Output> {
    public static final String id = Compile.class.getCanonicalName();

    public static class Input implements Serializable {
        public final String mainModuleName;
        public final Function<String, IStrategoTerm> astFunc;
        public final Resource outputDir;

        public Input(String mainModuleName, Function<String, IStrategoTerm> astFunc, Resource outputDir) {
            this.mainModuleName = mainModuleName;
            this.astFunc = astFunc;
            this.outputDir = outputDir;
        }
    }

    public static class Output implements Serializable {
        public final List<Resource> resultFiles;

        public Output(List<Resource> resultFiles) {
            this.resultFiles = resultFiles;
        }
    }

    @Override public Output exec(ExecContext context, Input input) {
        final List<Resource> resultFiles = new ArrayList<>();
        return new Output(resultFiles);
    }

    @Override public String getId() {
        return id;
    }
}
