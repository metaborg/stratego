package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.Set;

import mb.stratego.build.strincr.data.StrategySignature;

public class AnnoDefs implements Serializable {
    public final Set<StrategySignature> internalStrategySigs;
    public final Set<StrategySignature> externalStrategySigs;

    public AnnoDefs(Set<StrategySignature> internalStrategySigs,
        Set<StrategySignature> externalStrategySigs) {
        this.internalStrategySigs = internalStrategySigs;
        this.externalStrategySigs = externalStrategySigs;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        AnnoDefs that = (AnnoDefs) o;

        if(!internalStrategySigs.equals(that.internalStrategySigs))
            return false;
        return externalStrategySigs.equals(that.externalStrategySigs);
    }

    @Override public int hashCode() {
        int result = internalStrategySigs.hashCode();
        result = 31 * result + externalStrategySigs.hashCode();
        return result;
    }
}
