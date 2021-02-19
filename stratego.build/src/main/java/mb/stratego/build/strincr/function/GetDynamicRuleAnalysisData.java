package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;

public class GetDynamicRuleAnalysisData<T extends Set<StrategyAnalysisData> & Serializable>
    implements Function<CheckModuleOutput, T>, Serializable {
    public final StrategySignature strategySignature;

    public GetDynamicRuleAnalysisData(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @SuppressWarnings("unchecked") @Override public T apply(CheckModuleOutput output) {
        final T result = (T) new HashSet<StrategyAnalysisData>();
        for(StrategySignature signature : output.dynamicRules
            .getOrDefault(strategySignature, Collections.emptySet())) {
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

        GetDynamicRuleAnalysisData<?> that = (GetDynamicRuleAnalysisData<?>) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
