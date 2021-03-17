package mb.stratego.build.strincr.function;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.function.output.CongruenceGlobalIndex;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ToCongruenceGlobalIndex implements
    SerializableFunction<GlobalData, CongruenceGlobalIndex> {
    public static final ToCongruenceGlobalIndex INSTANCE = new ToCongruenceGlobalIndex();

    private ToCongruenceGlobalIndex() {
    }

    @Override public CongruenceGlobalIndex apply(GlobalData globalData) {
        return globalData.getCongruenceGlobalIndex();
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
