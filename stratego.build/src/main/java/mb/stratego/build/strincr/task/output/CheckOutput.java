package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;

public class CheckOutput implements Serializable {
    public final LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>>
        dynamicRuleIndex;
    public final ArrayList<Message> messages;
    public final boolean containsErrors; // derived from messages

    public CheckOutput(
        LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>> dynamicRuleIndex,
        ArrayList<Message> messages, boolean containsErrors) {
        this.dynamicRuleIndex = dynamicRuleIndex;
        this.messages = messages;
        this.containsErrors = containsErrors;
    }

    public CheckOutput(LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>> dynamicRuleIndex,
        ArrayList<Message> messages) {
        this(dynamicRuleIndex, messages,
            messages.stream().anyMatch(m -> m.severity == MessageSeverity.ERROR));
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckOutput output = (CheckOutput) o;

        if(!dynamicRuleIndex.equals(output.dynamicRuleIndex))
            return false;
        return messages.equals(output.messages);
    }

    @Override public int hashCode() {
        int result = dynamicRuleIndex.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Check.Output(" + messages.size() + ", " + containsErrors + ")";
    }

}
