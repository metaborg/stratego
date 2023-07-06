package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.data.StrategySignature;

public class AnnoDefs implements Serializable {
    public final LinkedHashSet<StrategySignature> internalStrategySigs;
    public final LinkedHashSet<StrategySignature> externalStrategySigs;
    public final long lastModified;

    public AnnoDefs(LinkedHashSet<StrategySignature> internalStrategySigs,
        LinkedHashSet<StrategySignature> externalStrategySigs, long lastModified) {
        this.internalStrategySigs = internalStrategySigs;
        this.externalStrategySigs = externalStrategySigs;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        AnnoDefs that = (AnnoDefs) o;

        if(lastModified != that.lastModified)
            return false;
        if(!internalStrategySigs.equals(that.internalStrategySigs))
            return false;
        return externalStrategySigs.equals(that.externalStrategySigs);
    }

    @Override public int hashCode() {
        int result = internalStrategySigs.hashCode();
        result = 31 * result + externalStrategySigs.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "AnnoDefs(" + internalStrategySigs
            + ", " + externalStrategySigs + ", " + lastModified + ')';
    }
}
