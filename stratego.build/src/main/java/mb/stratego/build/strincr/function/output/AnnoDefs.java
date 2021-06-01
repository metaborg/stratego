package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.data.StrategySignature;

public class AnnoDefs implements Serializable {
    public final LinkedHashSet<StrategySignature> internalStrategySigs;
    public final LinkedHashSet<StrategySignature> externalStrategySigs;

    public AnnoDefs(LinkedHashSet<StrategySignature> internalStrategySigs,
        LinkedHashSet<StrategySignature> externalStrategySigs) {
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

    @Override public String toString() {
        return "AnnoDefs(" + internalStrategySigs
            + ", " + externalStrategySigs + ')';
    }
}
