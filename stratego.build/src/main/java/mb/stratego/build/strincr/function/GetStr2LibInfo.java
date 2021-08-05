package mb.stratego.build.strincr.function;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.function.output.Str2LibInfo;
import mb.stratego.build.strincr.task.output.GlobalData;

public class GetStr2LibInfo implements SerializableFunction<GlobalData, Str2LibInfo> {
    public static final GetStr2LibInfo INSTANCE = new GetStr2LibInfo();


    @Override public Str2LibInfo apply(GlobalData globalData) {
        return new Str2LibInfo(globalData.nonExternalSorts, globalData.nonExternalConstructors,
            globalData.strategyTypes);
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
