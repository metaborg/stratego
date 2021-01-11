package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Collection;

public class GlobalIndex implements Serializable {
    public final Collection<ConstructorSignature> constructors;
    public final Collection<StrategySignature> strategies;

    public GlobalIndex(Collection<ConstructorSignature> constructors,
        Collection<StrategySignature> strategies) {
        this.constructors = constructors;
        this.strategies = strategies;
    }
}
