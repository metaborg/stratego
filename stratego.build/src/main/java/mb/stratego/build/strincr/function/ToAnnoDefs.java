package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import mb.stratego.build.strincr.function.output.AnnoDefs;
import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.strincr.data.StrategySignature;

public class ToAnnoDefs implements Function<GlobalData, AnnoDefs>, Serializable {
    public final Set<StrategySignature> filter;

    public ToAnnoDefs(Set<StrategySignature> filter) {
        this.filter = filter;
    }

    @Override public AnnoDefs apply(GlobalData globalData) {
        // Assumption: filter.size() > internalStrategyData.size() + externalStrategyData.size()
        final Set<StrategySignature> internalStrategyData = new HashSet<>();
        for(StrategySignature strategySignature : globalData.internalStrategies) {
            if(filter.contains(strategySignature)) {
                internalStrategyData.add(strategySignature);
            }
        }
        final Set<StrategySignature> externalStrategyData = new HashSet<>();
        for(StrategySignature strategySignature : globalData.externalStrategies) {
            if(filter.contains(strategySignature)) {
                externalStrategyData.add(strategySignature);
            }
        }
        return new AnnoDefs(internalStrategyData, externalStrategyData);
    }
}
