package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.task.output.CheckOutput;

public class ModulesDefiningDynamicRule<T extends Set<IModuleImportService.ModuleIdentifier> & Serializable>
    implements Serializable, Function<CheckOutput, T> {
    public final StrategySignature strategySignature;

    public ModulesDefiningDynamicRule(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @SuppressWarnings("unchecked") @Override public T apply(CheckOutput output) {
        return (T) output.dynamicRuleIndex.get(strategySignature);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModulesDefiningDynamicRule<?> that = (ModulesDefiningDynamicRule<?>) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
