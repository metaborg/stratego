package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class NormalDefinitions implements Serializable {
    public final Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData;
    public final long lastModified;

    public NormalDefinitions(Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData,
        long lastModified) {
        this.normalStrategyData = normalStrategyData;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        NormalDefinitions that = (NormalDefinitions) o;

        if(lastModified != that.lastModified)
            return false;
        return normalStrategyData.equals(that.normalStrategyData);
    }

    @Override public int hashCode() {
        int result = normalStrategyData.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }
}
