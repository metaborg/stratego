package mb.stratego.build.strincr.function;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.function.output.ModuleIndex;
import mb.stratego.build.strincr.task.output.ModuleData;

public class ToModuleIndex implements SerializableFunction<ModuleData, ModuleIndex> {
    public static final ToModuleIndex INSTANCE = new ToModuleIndex();

    private ToModuleIndex() {
    }

    @Override public ModuleIndex apply(ModuleData moduleData) {
        final LinkedHashSet<StrategyFrontData> strategies = new LinkedHashSet<>();
        for(LinkedHashSet<StrategyFrontData> strategyFrontData : moduleData.normalStrategyData.values()) {
            strategies.addAll(strategyFrontData);
        }
        final LinkedHashSet<ConstructorData> constrData = new LinkedHashSet<>();
        for(ArrayList<ConstructorData> data : moduleData.constrData.values()) {
            constrData.addAll(data);
        }
        return new ModuleIndex(moduleData.imports, moduleData.sortData, constrData, moduleData.injections,
            moduleData.externalSortData, new LinkedHashSet<>(moduleData.externalConstrData.keySet()), strategies,
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
