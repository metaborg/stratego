package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;

public class CheckModuleOutput implements Serializable {
    public final HashMap<StrategySignature, HashSet<StrategyAnalysisData>> strategyDataWithCasts;
    public final HashMap<StrategySignature, HashSet<StrategySignature>> dynamicRules;
    public final ArrayList<Message<?>> messages;

    public CheckModuleOutput(
        HashMap<StrategySignature, HashSet<StrategyAnalysisData>> strategyDataWithCasts,
        HashMap<StrategySignature, HashSet<StrategySignature>> dynamicRules,
        ArrayList<Message<?>> messages) {
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
