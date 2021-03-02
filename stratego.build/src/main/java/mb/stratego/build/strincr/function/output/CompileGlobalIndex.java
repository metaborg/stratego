package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.data.StrategySignature;

public class CompileGlobalIndex implements Serializable {
    public final LinkedHashSet<StrategySignature> nonExternalStrategies;
    public final LinkedHashSet<StrategySignature> dynamicRules;

    public CompileGlobalIndex(LinkedHashSet<StrategySignature> nonExternalStrategies,
        LinkedHashSet<StrategySignature> dynamicRules) {
        this.nonExternalStrategies = nonExternalStrategies;
        this.dynamicRules = dynamicRules;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileGlobalIndex that = (CompileGlobalIndex) o;

        if(!nonExternalStrategies.equals(that.nonExternalStrategies))
            return false;
        return dynamicRules.equals(that.dynamicRules);
    }

    @Override public int hashCode() {
        int result = nonExternalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalIndex(" + nonExternalStrategies + ", " + dynamicRules + ')';
    }
}
