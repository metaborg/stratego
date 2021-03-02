package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.function.Function;

import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.output.AnnoDefs;
import mb.stratego.build.strincr.task.output.GlobalData;

public class ToAnnoDefs implements Function<GlobalData, AnnoDefs>, Serializable {
    public final HashSet<StrategySignature> filter;

    public ToAnnoDefs(HashSet<StrategySignature> filter) {
        this.filter = filter;
    }

    @Override public AnnoDefs apply(GlobalData globalData) {
        // Assumption: filter.size() > internalStrategyData.size() + externalStrategyData.size()
        final LinkedHashSet<StrategySignature> internalStrategyData = new LinkedHashSet<>();
        for(StrategySignature strategySignature : globalData.internalStrategies) {
            if(filter.contains(strategySignature)) {
                internalStrategyData.add(strategySignature);
            }
        }
        final LinkedHashSet<StrategySignature> externalStrategyData = new LinkedHashSet<>();
        for(StrategySignature strategySignature : globalData.externalStrategies) {
            if(filter.contains(strategySignature)) {
                externalStrategyData.add(strategySignature);
            }
        }
        return new AnnoDefs(internalStrategyData, externalStrategyData);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ToAnnoDefs that = (ToAnnoDefs) o;

        return filter.equals(that.filter);
    }

    @Override public int hashCode() {
        return filter.hashCode();
    }
}
