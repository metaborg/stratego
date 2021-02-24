package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.HashSet;
import java.util.function.Function;

import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.strincr.function.output.ModuleIndex;

public class ToModuleIndex implements Function<ModuleData, ModuleIndex>, Serializable {
    public static final ToModuleIndex INSTANCE = new ToModuleIndex();

    private ToModuleIndex() {
    }

    @Override public ModuleIndex apply(ModuleData moduleData) {
        return new ModuleIndex(moduleData.imports, new HashSet<>(moduleData.constrData.keySet()),
            moduleData.injections,
            new HashSet<>(moduleData.externalConstrData.keySet()),
            new HashSet<>(moduleData.normalStrategyData.keySet()),
            new HashSet<>(moduleData.internalStrategyData.keySet()),
            new HashSet<>(moduleData.externalStrategyData.keySet()), moduleData.dynamicRules,
            moduleData.overlayData, moduleData.lastModified);
    }
}
