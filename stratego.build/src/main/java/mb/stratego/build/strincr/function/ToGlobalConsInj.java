package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.function.Function;

import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ToGlobalConsInj implements Function<GlobalData, GlobalConsInj>, Serializable {
    public static final ToGlobalConsInj INSTANCE = new ToGlobalConsInj();

    @Override public GlobalConsInj apply(GlobalData globalData) {
        return new GlobalConsInj(globalData.allModuleIdentifiers, globalData.nonExternalInjections, globalData.getGlobalIndex().nonExternalStrategies);
    }
}
