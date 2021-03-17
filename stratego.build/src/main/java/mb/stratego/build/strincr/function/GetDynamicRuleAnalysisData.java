package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;

public class GetDynamicRuleAnalysisData
    implements SerializableFunction<CheckModuleOutput, LinkedHashSet<StrategyAnalysisData>> {
    public final StrategySignature strategySignature;

    public GetDynamicRuleAnalysisData(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @Override public LinkedHashSet<StrategyAnalysisData> apply(CheckModuleOutput output) {
        final LinkedHashSet<StrategyAnalysisData> result = new LinkedHashSet<>();
        for(StrategySignature signature : output.dynamicRules
            .getOrDefault(strategySignature, new LinkedHashSet<>(0))) {
            final @Nullable Set<StrategyAnalysisData> analysisData =
                output.strategyDataWithCasts.get(signature);
            if(analysisData != null) {
                result.addAll(analysisData);
            }
        }
        return result;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GetDynamicRuleAnalysisData that = (GetDynamicRuleAnalysisData) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
