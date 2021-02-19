package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.util.WithLastModified;

/**
 * The information in the module data of a module as needed by the Resolve task for indexing.
 */
public class ModuleIndex implements Serializable, WithLastModified {
    public final List<IStrategoTerm> imports;
    public final Set<ConstructorSignature> constructors;
    public final Set<ConstructorSignature> externalConstructors;
    public final Set<StrategySignature> strategies;
    public final Set<StrategySignature> internalStrategies;
    public final Set<StrategySignature> externalStrategies;
    public final Set<StrategySignature> dynamicRules;
    public final Map<ConstructorSignature, List<OverlayData>> overlayData;
    public final long lastModified;

    public ModuleIndex(List<IStrategoTerm> imports, Set<ConstructorSignature> constructors,
        Set<ConstructorSignature> externalConstructors, Set<StrategySignature> strategies,
        Set<StrategySignature> internalStrategies, Set<StrategySignature> externalStrategies,
        Set<StrategySignature> dynamicRules,
        Map<ConstructorSignature, List<OverlayData>> overlayData, long lastModified) {
        this.imports = imports;
        this.constructors = constructors;
        this.externalConstructors = externalConstructors;
        this.strategies = strategies;
        this.internalStrategies = internalStrategies;
        this.externalStrategies = externalStrategies;
        this.dynamicRules = dynamicRules;
        this.overlayData = overlayData;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleIndex that = (ModuleIndex) o;

        if(lastModified != that.lastModified)
            return false;
        if(!imports.equals(that.imports))
            return false;
        if(!constructors.equals(that.constructors))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        if(!strategies.equals(that.strategies))
            return false;
        return overlayData.equals(that.overlayData);
    }

    @Override public int hashCode() {
        int result = imports.hashCode();
        result = 31 * result + constructors.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + strategies.hashCode();
        result = 31 * result + overlayData.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public long lastModified() {
        return lastModified;
    }
}
