package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;

public class CheckModuleOutput implements Serializable {
    public final LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>>
        strategyDataWithCasts;
    public final LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules;
    public final ArrayList<Message> messages;

    public CheckModuleOutput(
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>> strategyDataWithCasts,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules,
        ArrayList<Message> messages) {
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
        //@formatter:off
        return "CheckModuleOutput@" + System.identityHashCode(this) + '{'
            + "strategyDataWithCasts=" + strategyDataWithCasts.size()
            + ", dynamicRules=" + dynamicRules.size()
            + ", messages=" + messages.size()
            + '}';
        //@formatter:on
    }

}
