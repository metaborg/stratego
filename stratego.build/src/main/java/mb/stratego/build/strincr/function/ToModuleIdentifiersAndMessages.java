package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.function.Function;

import mb.stratego.build.strincr.function.output.ModuleIndentifiersAndMessages;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ToModuleIdentifiersAndMessages
    implements Function<GlobalData, ModuleIndentifiersAndMessages>, Serializable {
    public static final ToModuleIdentifiersAndMessages INSTANCE =
        new ToModuleIdentifiersAndMessages();

    private ToModuleIdentifiersAndMessages() {
    }

    @Override public ModuleIndentifiersAndMessages apply(GlobalData globalData) {
        return new ModuleIndentifiersAndMessages(globalData.allModuleIdentifiers,
            globalData.messages);
    }

    @Override public boolean equals(Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
