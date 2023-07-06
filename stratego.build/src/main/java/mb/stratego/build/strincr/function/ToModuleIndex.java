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
        for(ArrayList<StrategyFrontData> strategyFrontData : moduleData.normalStrategyData.values()) {
            strategies.addAll(strategyFrontData);
        }
        final LinkedHashSet<StrategyFrontData> externalStrategyData = new LinkedHashSet<>();
        for(ArrayList<StrategyFrontData> strategyFrontData : moduleData.externalStrategyData.values()) {
            externalStrategyData.addAll(strategyFrontData);
        }
        final LinkedHashSet<ConstructorData> nonOverlayConstructors = new LinkedHashSet<>();
        for(ArrayList<ConstructorData> data : moduleData.constrData.values()) {
            for(ConstructorData datum : data) {
                if(!datum.isOverlay) {
                    nonOverlayConstructors.add(datum);
                }
            }
        }
        return new ModuleIndex(moduleData.str2LibPackageNames, moduleData.imports,
            moduleData.sortData, moduleData.externalSortData, nonOverlayConstructors,
            moduleData.injections, new LinkedHashSet<>(moduleData.externalConstrData.keySet()),
            strategies, new LinkedHashSet<>(moduleData.internalStrategyData.keySet()),
            externalStrategyData, moduleData.dynamicRules, moduleData.overlayData,
            moduleData.overlayAsts, moduleData.overlayUsedConstrs, moduleData.messages,
            moduleData.lastModified);
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
