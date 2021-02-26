package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.function.Function;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.util.LastModified;

public class ToAst implements Function<ModuleData, LastModified<IStrategoTerm>>, Serializable {
    public static final ToAst INSTANCE = new ToAst();

    @Override public LastModified<IStrategoTerm> apply(ModuleData moduleData) {
        return new LastModified<>(moduleData.ast, moduleData.lastModified);
    }

    @Override public boolean equals(Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
