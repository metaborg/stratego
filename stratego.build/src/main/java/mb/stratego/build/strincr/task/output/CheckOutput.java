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
    public final LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers;
    public final ArrayList<Message> messages;
    public final boolean containsErrors; // derived from messages
    protected final int hashCode;

    public CheckOutput(
        LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>> dynamicRuleIndex,
        LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers,
        ArrayList<Message> messages, boolean containsErrors) {
        this.dynamicRuleIndex = dynamicRuleIndex;
        this.allModuleIdentifiers = allModuleIdentifiers;
        this.messages = messages;
        this.containsErrors = containsErrors;
        this.hashCode = hashFunction();
    }

    public CheckOutput(LinkedHashMap<StrategySignature, LinkedHashSet<IModuleImportService.ModuleIdentifier>> dynamicRuleIndex,
        LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers,
        ArrayList<Message> messages) {
        this(dynamicRuleIndex, allModuleIdentifiers, messages,
            messages.stream().anyMatch(m -> m.severity == MessageSeverity.ERROR));
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckOutput output = (CheckOutput) o;

        if(hashCode != output.hashCode)
            return false;
        if(!dynamicRuleIndex.equals(output.dynamicRuleIndex))
            return false;
        if(!allModuleIdentifiers.equals(output.allModuleIdentifiers))
            return false;
        return messages.equals(output.messages);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = dynamicRuleIndex.hashCode();
        result = 31 * result + allModuleIdentifiers.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "CheckOutput@" + System.identityHashCode(this) + '{'
            + "dynamicRuleIndex=" + dynamicRuleIndex.size()
            + ", allModuleIdentifiers=" + allModuleIdentifiers.size()
            + ", messages=" + messages.size()
            + ", containsErrors=" + containsErrors
            + '}';
        //@formatter:on
    }

}
