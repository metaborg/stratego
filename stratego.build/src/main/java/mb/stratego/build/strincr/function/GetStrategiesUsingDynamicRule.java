package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;

public class GetStrategiesUsingDynamicRule
    implements SerializableFunction<CheckModuleOutput, LinkedHashSet<StrategySignature>> {
    public final StrategySignature strategySignature;

    public GetStrategiesUsingDynamicRule(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @Override public LinkedHashSet<StrategySignature> apply(CheckModuleOutput output) {
        return output.dynamicRules
            .getOrDefault(strategySignature, new LinkedHashSet<>(0));
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GetStrategiesUsingDynamicRule that = (GetStrategiesUsingDynamicRule) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
