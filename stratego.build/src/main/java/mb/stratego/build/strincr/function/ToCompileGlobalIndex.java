package mb.stratego.build.strincr.function;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.function.output.CompileGlobalIndex;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ToCompileGlobalIndex implements SerializableFunction<GlobalData, CompileGlobalIndex> {
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
