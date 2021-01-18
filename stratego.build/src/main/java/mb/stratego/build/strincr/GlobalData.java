package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import mb.pie.api.STask;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.message.Message2;

public class GlobalData implements Serializable {
    public final Set<ModuleIdentifier> allModuleIdentifiers;
    public final Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex;
    public final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex;
    public final Map<String, Set<ModuleIdentifier>> ambStrategyIndex;
    public final Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex;
    public final List<Message2<?>> messages;

    public GlobalData(Set<ModuleIdentifier> allModuleIdentifiers,
        Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex,
        Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex,
        Map<String, Set<ModuleIdentifier>> ambStrategyIndex,
        Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex, List<Message2<?>> messages) {
        this.allModuleIdentifiers = allModuleIdentifiers;
        this.constructorIndex = constructorIndex;
        this.strategyIndex = strategyIndex;
        this.ambStrategyIndex = ambStrategyIndex;
        this.overlayIndex = overlayIndex;
        this.messages = messages;
    }

    public static class ToGlobalIndex implements Function<GlobalData, GlobalIndex>, Serializable {
        public static final ToGlobalIndex Instance = new ToGlobalIndex();

        private ToGlobalIndex() {
        }

        @Override public GlobalIndex apply(GlobalData globalData) {
            return new GlobalIndex(new HashSet<>(globalData.constructorIndex.keySet()),
                new HashSet<>(globalData.strategyIndex.keySet()));
        }
    }

    public static class AllModulesIdentifiers<T extends Set<ModuleIdentifier> & Serializable>
        implements Function<GlobalData, T>, Serializable {
        public static final AllModulesIdentifiers<?> Instance = new AllModulesIdentifiers<>();

        private AllModulesIdentifiers() {
        }

        @Override public T apply(GlobalData globalData) {
            //noinspection unchecked
            return (T) globalData.allModuleIdentifiers;
        }
    }

    public static class ModulesDefiningStrategy<T extends Set<ModuleIdentifier> & Serializable>
        implements Function<GlobalData, T>, Serializable {
        public final StrategySignature strategySignature;

        public ModulesDefiningStrategy(StrategySignature strategySignature) {
            this.strategySignature = strategySignature;
        }

        @Override public T apply(GlobalData globalData) {
            //noinspection unchecked
            return (T) globalData.strategyIndex
                .getOrDefault(strategySignature, Collections.emptySet());
        }
    }
}
