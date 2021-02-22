package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;

public class CheckModuleOutput implements Serializable {
    public final Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts;
    public final Map<StrategySignature, Set<StrategySignature>> dynamicRules;
    public final List<Message<?>> messages;

    public CheckModuleOutput(
        Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts,
        Map<StrategySignature, Set<StrategySignature>> dynamicRules, List<Message<?>> messages) {
        this.strategyDataWithCasts = strategyDataWithCasts;
        this.dynamicRules = dynamicRules;
        this.messages = messages;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckModuleOutput output = (CheckModuleOutput) o;

        if(!strategyDataWithCasts.equals(output.strategyDataWithCasts))
            return false;
        if(!dynamicRules.equals(output.dynamicRules))
            return false;
        return messages.equals(output.messages);
    }

    @Override public int hashCode() {
        int result = strategyDataWithCasts.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "CheckModule.Output(" + messages.size() + ")";
    }

}
