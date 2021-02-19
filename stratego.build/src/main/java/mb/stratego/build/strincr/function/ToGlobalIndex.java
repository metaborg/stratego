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
}
