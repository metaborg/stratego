package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.function.Function;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ModulesDefiningStrategy
    implements Function<GlobalData, LinkedHashSet<IModuleImportService.ModuleIdentifier>>,
    Serializable {
    public final StrategySignature strategySignature;

    public ModulesDefiningStrategy(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @Override
    public LinkedHashSet<IModuleImportService.ModuleIdentifier> apply(GlobalData globalData) {
        return globalData.strategyIndex.getOrDefault(strategySignature, new LinkedHashSet<>(0));
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModulesDefiningStrategy that = (ModulesDefiningStrategy) o;

        return strategySignature.equals(that.strategySignature);
    }

    @Override public int hashCode() {
        return strategySignature.hashCode();
    }
}
