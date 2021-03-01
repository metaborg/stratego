package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.function.Function;

import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.function.output.CompileGlobalIndex;

public class ToCompileGlobalIndex implements Function<GlobalData, CompileGlobalIndex>, Serializable {
    public static final ToCompileGlobalIndex INSTANCE = new ToCompileGlobalIndex();

    private ToCompileGlobalIndex() {
    }

    @Override public CompileGlobalIndex apply(GlobalData globalData) {
        return globalData.getCompileGlobalIndex();
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
