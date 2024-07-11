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
    public final LinkedHashSet<StrategySignature> strategiesDefinedByModule;
    public final ArrayList<Message> messages;
    protected final int hashCode;

    public CheckModuleOutput(
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategyAnalysisData>> strategyDataWithCasts,
        LinkedHashMap<StrategySignature, LinkedHashSet<StrategySignature>> dynamicRules,
        LinkedHashSet<StrategySignature> strategiesDefinedByModule, ArrayList<Message> messages) {
        this.strategyDataWithCasts = strategyDataWithCasts;
        this.dynamicRules = dynamicRules;
        this.strategiesDefinedByModule = strategiesDefinedByModule;
        this.messages = messages;
        this.hashCode = hashFunction();
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckModuleOutput output = (CheckModuleOutput) o;

        if(hashCode != output.hashCode)
            return false;
        if(!strategyDataWithCasts.equals(output.strategyDataWithCasts))
            return false;
        if(!dynamicRules.equals(output.dynamicRules))
            return false;
        if(!strategiesDefinedByModule.equals(output.strategiesDefinedByModule))
            return false;
        return messages.equals(output.messages);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = strategyDataWithCasts.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + strategiesDefinedByModule.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "CheckModuleOutput@" + System.identityHashCode(this) + '{'
            + "strategyDataWithCasts=" + strategyDataWithCasts.size()
            + ", dynamicRules=" + dynamicRules.size()
            + ", strategiesDefinedByModule=" + strategiesDefinedByModule.size()
            + ", messages=" + messages.size()
            + '}';
        //@formatter:on
    }

}
