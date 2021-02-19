package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;

public class ModulesDefiningStrategy<T extends Set<IModuleImportService.ModuleIdentifier> & Serializable>
    implements Function<GlobalData, T>, Serializable {
    public final StrategySignature strategySignature;

    public ModulesDefiningStrategy(StrategySignature strategySignature) {
        this.strategySignature = strategySignature;
    }

    @SuppressWarnings("unchecked") @Override public T apply(GlobalData globalData) {
        return (T) globalData.strategyIndex.getOrDefault(strategySignature, Collections.emptySet());
    }
}
