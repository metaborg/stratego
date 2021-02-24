package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;

public class GetDynamicRuleAnalysisData
    implements Function<CheckModuleOutput, HashSet<StrategyAnalysisData>>, Serializable {
    public final StrategySignature strategySignature;

    public GetDynamicRuleAnalysisData(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @Override public HashSet<StrategyAnalysisData> apply(CheckModuleOutput output) {
        final HashSet<StrategyAnalysisData> result = new HashSet<>();
        for(StrategySignature signature : output.dynamicRules
            .getOrDefault(strategySignature, new HashSet<>(0))) {
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
