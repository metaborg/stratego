package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.function.Function;

import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ToGlobalConsInj implements Function<GlobalData, GlobalConsInj>, Serializable {
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
