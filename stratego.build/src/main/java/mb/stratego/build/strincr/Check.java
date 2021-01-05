package mb.stratego.build.strincr;

import java.io.Serializable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Function;
import mb.pie.api.TaskDef;

public class Check implements TaskDef<Check.Input, Check.Output> {
    public static final String id = Check.class.getCanonicalName();

    public static class Input implements Serializable {
        public final String mainModuleName;
        public final Function<String, IStrategoTerm> astFunc;

        public Input(String mainModuleName, Function<String, IStrategoTerm> astFunc) {
            this.mainModuleName = mainModuleName;
            this.astFunc = astFunc;
        }
    }

    public static class Output implements Serializable {
    }

    @Override
    public Output exec(ExecContext context, Input input) throws ExecException {
        return new Output();
    }

    @Override
    public String getId() {
        return id;
    }
}
