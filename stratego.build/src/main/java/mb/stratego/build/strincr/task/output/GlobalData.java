package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.function.output.GlobalIndex;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.Message2;

public class GlobalData implements Serializable {
    public final Set<ModuleIdentifier> allModuleIdentifiers;
    public final Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex;
    public final Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex;
    public final Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex;
    public final Set<ConstructorSignature> externalConstructors;
    public final Set<StrategySignature> internalStrategies;
    public final Set<StrategySignature> externalStrategies;
    public final Set<StrategySignature> dynamicRules;
    public final List<Message2<?>> messages;
    private transient @Nullable GlobalIndex globalIndex = null;

    public GlobalData(Set<ModuleIdentifier> allModuleIdentifiers,
        Map<ConstructorSignature, Set<ModuleIdentifier>> constructorIndex,
        Map<StrategySignature, Set<ModuleIdentifier>> strategyIndex,
        Map<ConstructorSignature, Set<ModuleIdentifier>> overlayIndex,
        Set<ConstructorSignature> externalConstructors, Set<StrategySignature> internalStrategies,
        Set<StrategySignature> externalStrategies, Set<StrategySignature> dynamicRules,
        List<Message2<?>> messages) {
        this.allModuleIdentifiers = allModuleIdentifiers;
        this.constructorIndex = constructorIndex;
        this.strategyIndex = strategyIndex;
        this.overlayIndex = overlayIndex;
        this.externalConstructors = externalConstructors;
        this.internalStrategies = internalStrategies;
        this.externalStrategies = externalStrategies;
        this.dynamicRules = dynamicRules;
        this.messages = messages;
    }

    public GlobalIndex getGlobalIndex() {
        if(globalIndex == null) {
            final HashSet<StrategySignature> nonExternalStrategies =
                new HashSet<>(strategyIndex.keySet());
            nonExternalStrategies.removeAll(externalStrategies);
            nonExternalStrategies.addAll(internalStrategies);
            final HashSet<ConstructorSignature> nonExternalConstructors =
                new HashSet<>(constructorIndex.keySet());
            nonExternalConstructors.removeAll(externalConstructors);
            globalIndex =
                new GlobalIndex(nonExternalConstructors, externalConstructors, nonExternalStrategies, dynamicRules);
        }
        return globalIndex;
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
        if(!overlayIndex.equals(that.overlayIndex))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        if(!internalStrategies.equals(that.internalStrategies))
            return false;
        if(!externalStrategies.equals(that.externalStrategies))
            return false;
        if(!dynamicRules.equals(that.dynamicRules))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = allModuleIdentifiers.hashCode();
        result = 31 * result + constructorIndex.hashCode();
        result = 31 * result + strategyIndex.hashCode();
        result = 31 * result + overlayIndex.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + internalStrategies.hashCode();
        result = 31 * result + externalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }

}
