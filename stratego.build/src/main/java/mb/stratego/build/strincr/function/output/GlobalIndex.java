package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.Collection;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.StrategySignature;

public class GlobalIndex implements Serializable {
    public final Collection<ConstructorSignature> nonExternalConstructors;
    public final Collection<ConstructorSignature> externalConstructors;
    public final Collection<StrategySignature> nonExternalStrategies;
    public final Collection<StrategySignature> dynamicRules;

    public GlobalIndex(Collection<ConstructorSignature> nonExternalConstructors,
        Collection<ConstructorSignature> externalConstructors,
        Collection<StrategySignature> nonExternalStrategies,
        Collection<StrategySignature> dynamicRules) {
        this.nonExternalConstructors = nonExternalConstructors;
        this.externalConstructors = externalConstructors;
        this.nonExternalStrategies = nonExternalStrategies;
        this.dynamicRules = dynamicRules;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GlobalIndex that = (GlobalIndex) o;

        if(!nonExternalConstructors.equals(that.nonExternalConstructors))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        if(!nonExternalStrategies.equals(that.nonExternalStrategies))
            return false;
        return dynamicRules.equals(that.dynamicRules);
    }

    @Override public int hashCode() {
        int result = nonExternalConstructors.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        return result;
    }
}
