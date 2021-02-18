package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collection;

public class GlobalIndex implements Serializable {
    public final Collection<ConstructorSignature> constructors;
    public final Collection<StrategySignature> nonExternalStrategies;
    public final Collection<StrategySignature> dynamicRules;

    public GlobalIndex(Collection<ConstructorSignature> constructors,
        Collection<StrategySignature> nonExternalStrategies,
        Collection<StrategySignature> dynamicRules) {
        this.constructors = constructors;
        this.nonExternalStrategies = nonExternalStrategies;
        this.dynamicRules = dynamicRules;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GlobalIndex that = (GlobalIndex) o;

        if(!constructors.equals(that.constructors))
            return false;
        if(!nonExternalStrategies.equals(that.nonExternalStrategies))
            return false;
        return dynamicRules.equals(that.dynamicRules);
    }

    @Override public int hashCode() {
        int result = constructors.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        return result;
    }
}
