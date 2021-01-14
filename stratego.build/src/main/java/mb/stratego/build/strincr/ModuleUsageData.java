package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.util.WithLastModified;

public class ModuleUsageData implements Serializable, WithLastModified {
    public final IStrategoTerm ast;
    public final Set<ConstructorSignature> usedConstructors;
    public final Set<StrategySignature> usedStrategies;
    public final Set<String> usedAmbiguousStrategies;
    public final long lastModified;

    public ModuleUsageData(IStrategoTerm ast, Set<ConstructorSignature> usedConstructors,
        Set<StrategySignature> usedStrategies, Set<String> usedAmbiguousStrategies,
        long lastModified) {
        this.ast = ast;
        this.usedConstructors = usedConstructors;
        this.usedStrategies = usedStrategies;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.lastModified = lastModified;
    }

    @Override public long lastModified() {
        return lastModified;
    }
}
