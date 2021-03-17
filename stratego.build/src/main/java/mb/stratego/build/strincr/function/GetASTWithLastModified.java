package mb.stratego.build.strincr.function;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.util.LastModified;

public class GetASTWithLastModified
    implements SerializableFunction<ModuleData, LastModified<IStrategoTerm>> {
    public static final GetASTWithLastModified INSTANCE = new GetASTWithLastModified();

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
