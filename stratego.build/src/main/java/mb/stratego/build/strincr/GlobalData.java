package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mb.pie.api.STask;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message;

public class GlobalData implements Serializable {
    public final Map<ModuleIdentifier, STask<ModuleData>> moduleDataTasks;
    public final Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex;
    public final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex;
    public final Map<String, Set<ModuleIdentifier>> ambStrategyIndex;
    public final Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex;
    public final List<Message<?>> messages;

    public GlobalData(Map<ModuleIdentifier, STask<ModuleData>> moduleDataTasks,
        Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex,
        Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex,
        Map<String, Set<ModuleIdentifier>> ambStrategyIndex, Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex, List<Message<?>> messages) {
        this.moduleDataTasks = moduleDataTasks;
        this.constructorIndex = constructorIndex;
        this.strategyIndex = strategyIndex;
        this.ambStrategyIndex = ambStrategyIndex;
        this.overlayIndex = overlayIndex;
        this.messages = messages;
    }

    public GlobalIndex toGlobalIndex() {
        return new GlobalIndex(constructorIndex.keySet(), strategyIndex.keySet());
    }

    public <T extends Set<ModuleIdentifier> & Serializable> T modulesDefiningStrategy(
        StrategySignature strategySignature) {
        //noinspection unchecked
        return (T) strategyIndex.getOrDefault(strategySignature, Collections.emptySet());
    }
}
