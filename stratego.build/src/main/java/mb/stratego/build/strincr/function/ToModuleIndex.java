package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.function.Function;

import mb.stratego.build.strincr.function.output.ModuleIndex;
import mb.stratego.build.strincr.task.output.ModuleData;

public class ToModuleIndex implements Function<ModuleData, ModuleIndex>, Serializable {
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
            moduleData.overlayData, moduleData.lastModified);
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
