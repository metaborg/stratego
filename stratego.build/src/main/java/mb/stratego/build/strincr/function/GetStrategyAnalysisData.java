package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;

public class GetStrategyAnalysisData<T extends Set<StrategyAnalysisData> & Serializable>
    implements Function<CheckModuleOutput, T>, Serializable {
    public final StrategySignature strategySignature;

    public GetStrategyAnalysisData(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @SuppressWarnings("unchecked") @Override public T apply(CheckModuleOutput output) {
        return (T) output.strategyDataWithCasts
            .getOrDefault(strategySignature, Collections.emptySet());
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GetStrategyAnalysisData<?> that = (GetStrategyAnalysisData<?>) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
