package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.task.output.CheckOutput;

public class ModulesDefiningDynamicRule implements
    SerializableFunction<CheckOutput, LinkedHashSet<IModuleImportService.ModuleIdentifier>> {
    public final StrategySignature strategySignature;

    public ModulesDefiningDynamicRule(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @Override
    public LinkedHashSet<IModuleImportService.ModuleIdentifier> apply(CheckOutput output) {
        return output.dynamicRuleIndex.getOrDefault(strategySignature, new LinkedHashSet<>(0));
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModulesDefiningDynamicRule that = (ModulesDefiningDynamicRule) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
