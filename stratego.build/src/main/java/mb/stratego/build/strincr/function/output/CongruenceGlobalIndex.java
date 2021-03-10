package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.StrategySignature;

public class CongruenceGlobalIndex implements Serializable {
    public final LinkedHashSet<ConstructorSignature> nonExternalConstructors;
    public final LinkedHashSet<ConstructorSignature> externalConstructors;
    public final LinkedHashSet<StrategySignature> nonExternalStrategies;

    public CongruenceGlobalIndex(LinkedHashSet<ConstructorSignature> nonExternalConstructors,
        LinkedHashSet<ConstructorSignature> externalConstructors,
        LinkedHashSet<StrategySignature> nonExternalStrategies) {
        this.nonExternalConstructors = nonExternalConstructors;
        this.externalConstructors = externalConstructors;
        this.nonExternalStrategies = nonExternalStrategies;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CongruenceGlobalIndex that = (CongruenceGlobalIndex) o;

        if(!nonExternalConstructors.equals(that.nonExternalConstructors))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        return nonExternalStrategies.equals(that.nonExternalStrategies);
    }

    @Override public int hashCode() {
        int result = nonExternalConstructors.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalIndex(" + nonExternalConstructors + ", " + externalConstructors + ", "
            + nonExternalStrategies + ')';
    }
}
