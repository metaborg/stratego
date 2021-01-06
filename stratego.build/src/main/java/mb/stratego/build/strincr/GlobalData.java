package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mb.pie.api.STask;
import mb.stratego.build.strincr.message.Message;

public class GlobalData implements Serializable {
    public final Map<IModuleImportService.ModuleIdentifier, STask<ModuleData>> moduleDataTasks;
    public final Map<ConstructorSignature, Set<IModuleImportService.ModuleIdentifier>>
        constructorIndex;
    public final Map<StrategySignature, Set<IModuleImportService.ModuleIdentifier>> strategyIndex;
    public final Map<ConstructorSignature, Set<IModuleImportService.ModuleIdentifier>> overlayIndex;
    public final List<Message<?>> messages;

    public GlobalData(Map<IModuleImportService.ModuleIdentifier, STask<ModuleData>> moduleDataTasks,
        Map<ConstructorSignature, Set<IModuleImportService.ModuleIdentifier>> constructorIndex,
        Map<StrategySignature, Set<IModuleImportService.ModuleIdentifier>> strategyIndex,
        Map<ConstructorSignature, Set<IModuleImportService.ModuleIdentifier>> overlayIndex,
        List<Message<?>> messages) {
        this.moduleDataTasks = moduleDataTasks;
        this.constructorIndex = constructorIndex;
        this.strategyIndex = strategyIndex;
        this.overlayIndex = overlayIndex;
        this.messages = messages;
    }
}
