package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Set;

public class ModuleAnnoDefs implements Serializable {
    public final Set<StrategySignature> internalStrategySigs;
    public final Set<StrategySignature> externalStrategySigs;

    public ModuleAnnoDefs(Set<StrategySignature> internalStrategySigs,
        Set<StrategySignature> externalStrategySigs) {
        this.internalStrategySigs = internalStrategySigs;
        this.externalStrategySigs = externalStrategySigs;
    }
}
