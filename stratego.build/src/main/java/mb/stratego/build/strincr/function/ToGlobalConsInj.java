package mb.stratego.build.strincr.function;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ToGlobalConsInj implements SerializableFunction<GlobalData, GlobalConsInj> {
    public static final ToGlobalConsInj INSTANCE = new ToGlobalConsInj();

    @Override public GlobalConsInj apply(GlobalData globalData) {
        return globalData.getGlobalConsInj();
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
