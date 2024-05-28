package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;

import jakarta.annotation.Nullable;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;

public class GetStrategyAnalysisData
    implements SerializableFunction<CheckModuleOutput, LinkedHashSet<StrategyAnalysisData>> {
    public final StrategySignature strategySignature;

    public GetStrategyAnalysisData(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @Override public LinkedHashSet<StrategyAnalysisData> apply(CheckModuleOutput output) {
        return output.strategyDataWithCasts.getOrDefault(strategySignature, new LinkedHashSet<>(0));
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GetStrategyAnalysisData that = (GetStrategyAnalysisData) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
