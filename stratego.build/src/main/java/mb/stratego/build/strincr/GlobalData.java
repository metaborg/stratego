package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

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

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GlobalData that = (GlobalData) o;

        if(!allModuleIdentifiers.equals(that.allModuleIdentifiers))
            return false;
        if(!constructorIndex.equals(that.constructorIndex))
            return false;
        if(!strategyIndex.equals(that.strategyIndex))
            return false;
        if(!ambStrategyIndex.equals(that.ambStrategyIndex))
            return false;
        if(!overlayIndex.equals(that.overlayIndex))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = allModuleIdentifiers.hashCode();
        result = 31 * result + constructorIndex.hashCode();
        result = 31 * result + strategyIndex.hashCode();
        result = 31 * result + ambStrategyIndex.hashCode();
        result = 31 * result + overlayIndex.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
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

        @SuppressWarnings("unchecked") @Override public T apply(GlobalData globalData) {
            return (T) globalData.allModuleIdentifiers;
        }
    }

    public static class ModulesDefiningStrategy<T extends Set<ModuleIdentifier> & Serializable>
        implements Function<GlobalData, T>, Serializable {
        public final StrategySignature strategySignature;

        public ModulesDefiningStrategy(StrategySignature strategySignature) {
            this.strategySignature = strategySignature;
        }

        @SuppressWarnings("unchecked") @Override public T apply(GlobalData globalData) {
            return (T) globalData.strategyIndex
                .getOrDefault(strategySignature, Collections.emptySet());
        }
    }

    public static class ModulesDefiningOverlays<T extends Set<ModuleIdentifier> & Serializable>
        implements Function<GlobalData, T>, Serializable {
        public final Set<ConstructorSignature> usedConstructors;

        public ModulesDefiningOverlays(Set<ConstructorSignature> usedConstructors) {
            this.usedConstructors = usedConstructors;
        }

        @SuppressWarnings("unchecked") @Override public T apply(GlobalData globalData) {
            final HashSet<ModuleIdentifier> result = new HashSet<>();
            for(ConstructorSignature usedConstructor : usedConstructors) {
                final @Nullable Set<ModuleIdentifier> moduleIdentifiers =
                    globalData.overlayIndex.get(usedConstructor);
                if(moduleIdentifiers != null) {
                    result.addAll(moduleIdentifiers);
                }
            }
            return (T) result;
        }
    }
}
