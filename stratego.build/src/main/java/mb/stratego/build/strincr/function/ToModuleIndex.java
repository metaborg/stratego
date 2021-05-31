package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.function.output.ModuleIndex;
import mb.stratego.build.strincr.task.output.ModuleData;

public class ToModuleIndex implements SerializableFunction<ModuleData, ModuleIndex> {
    public static final ToModuleIndex INSTANCE = new ToModuleIndex();

    private ToModuleIndex() {
    }

    @Override public ModuleIndex apply(ModuleData moduleData) {
        return new ModuleIndex(moduleData.imports,
            new LinkedHashSet<>(moduleData.constrData.keySet()), moduleData.injections,
            new LinkedHashSet<>(moduleData.externalConstrData.keySet()),
            new LinkedHashSet<>(moduleData.normalStrategyData.keySet()),
            new LinkedHashSet<>(moduleData.internalStrategyData.keySet()),
            new LinkedHashSet<>(moduleData.externalStrategyData.keySet()), moduleData.dynamicRules,
            moduleData.overlayData, moduleData.messages, moduleData.lastModified);
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
