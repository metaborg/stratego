package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collection;

public class GlobalIndex implements Serializable {
    public final Collection<ConstructorSignature> constructors;
    public final Collection<StrategySignature> nonExternalStrategies;

    public GlobalIndex(Collection<ConstructorSignature> constructors,
        Collection<StrategySignature> nonExternalStrategies) {
        this.constructors = constructors;
        this.nonExternalStrategies = nonExternalStrategies;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GlobalIndex that = (GlobalIndex) o;

        if(!constructors.equals(that.constructors))
            return false;
        return nonExternalStrategies.equals(that.nonExternalStrategies);
    }

    @Override public int hashCode() {
        int result = constructors.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        return result;
    }
}
