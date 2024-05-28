package mb.stratego.build.strincr.function;

import jakarta.annotation.Nullable;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.task.output.GlobalData;

public class GetStrategyType implements SerializableFunction<GlobalData, StrategyType> {
    public final StrategySignature strategySignature;

    public GetStrategyType(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @Override public @Nullable StrategyType apply(GlobalData globalData) {
        return globalData.strategyTypes.get(strategySignature);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GetStrategyType that = (GetStrategyType) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}