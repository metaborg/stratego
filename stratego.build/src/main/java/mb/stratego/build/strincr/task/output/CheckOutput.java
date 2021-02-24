package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mb.pie.api.STask;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message;

public class CheckOutput implements Serializable {
    public final HashMap<IModuleImportService.ModuleIdentifier, STask<CheckModuleOutput>>
        moduleCheckTasks;
    public final HashMap<StrategySignature, HashSet<IModuleImportService.ModuleIdentifier>>
        strategyIndex;
    public final HashMap<StrategySignature, HashSet<IModuleImportService.ModuleIdentifier>>
        dynamicRuleIndex;
    public final ArrayList<Message<?>> messages;
    public final boolean containsErrors;

    public CheckOutput(
        HashMap<IModuleImportService.ModuleIdentifier, STask<CheckModuleOutput>> moduleCheckTasks,
        HashMap<StrategySignature, HashSet<IModuleImportService.ModuleIdentifier>> strategyIndex,
        HashMap<StrategySignature, HashSet<IModuleImportService.ModuleIdentifier>> dynamicRuleIndex,
        ArrayList<Message<?>> messages, boolean containsErrors) {
        this.moduleCheckTasks = moduleCheckTasks;
        this.strategyIndex = strategyIndex;
        this.dynamicRuleIndex = dynamicRuleIndex;
        this.messages = messages;
        this.containsErrors = containsErrors;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckOutput output = (CheckOutput) o;

        if(!moduleCheckTasks.equals(output.moduleCheckTasks))
            return false;
        if(!strategyIndex.equals(output.strategyIndex))
            return false;
        if(!dynamicRuleIndex.equals(output.dynamicRuleIndex))
            return false;
        return messages.equals(output.messages);
    }

    @Override public int hashCode() {
        int result = moduleCheckTasks.hashCode();
        result = 31 * result + strategyIndex.hashCode();
        result = 31 * result + dynamicRuleIndex.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Check.Output(" + messages.size() + ", " + containsErrors + ")";
    }

}
