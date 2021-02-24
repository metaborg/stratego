package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.function.Function;

import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.function.output.GlobalIndex;

public class ToGlobalIndex implements Function<GlobalData, GlobalIndex>, Serializable {
    public static final ToGlobalIndex INSTANCE = new ToGlobalIndex();

    private ToGlobalIndex() {
    }

    @Override public GlobalIndex apply(GlobalData globalData) {
        return globalData.getGlobalIndex();
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
