package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.message.Message;

public class ModuleIndentifiersAndMessages implements Serializable {
    public final LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers;
    public final ArrayList<Message> messages;

    public ModuleIndentifiersAndMessages(
        LinkedHashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers,
        ArrayList<Message> messages) {
        this.allModuleIdentifiers = allModuleIdentifiers;
        this.messages = messages;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleIndentifiersAndMessages that = (ModuleIndentifiersAndMessages) o;

        if(!allModuleIdentifiers.equals(that.allModuleIdentifiers))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = allModuleIdentifiers.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ModuleIndentifiersAndMessages(" + allModuleIdentifiers + ", " + messages + ')';
    }
}
