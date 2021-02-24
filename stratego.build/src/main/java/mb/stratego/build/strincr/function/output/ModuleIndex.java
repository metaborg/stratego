package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.util.WithLastModified;

/**
 * The information in the module data of a module as needed by the Resolve task for indexing.
 */
public class ModuleIndex implements Serializable, WithLastModified {
    public final ArrayList<IStrategoTerm> imports;
    public final HashSet<ConstructorSignature> constructors;
    public final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections;
    public final HashSet<ConstructorSignature> externalConstructors;
    public final HashSet<StrategySignature> strategies;
    public final HashSet<StrategySignature> internalStrategies;
    public final HashSet<StrategySignature> externalStrategies;
    public final HashSet<StrategySignature> dynamicRules;
    public final HashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData;
    public final long lastModified;

    public ModuleIndex(ArrayList<IStrategoTerm> imports, HashSet<ConstructorSignature> constructors,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections,
        HashSet<ConstructorSignature> externalConstructors, HashSet<StrategySignature> strategies,
        HashSet<StrategySignature> internalStrategies,
        HashSet<StrategySignature> externalStrategies, HashSet<StrategySignature> dynamicRules,
        HashMap<ConstructorSignature, ArrayList<OverlayData>> overlayData, long lastModified) {
        this.imports = imports;
        this.constructors = constructors;
        this.injections = injections;
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
        if(!injections.equals(that.injections))
            return false;
        if(!externalConstructors.equals(that.externalConstructors))
            return false;
        if(!strategies.equals(that.strategies))
            return false;
        if(!internalStrategies.equals(that.internalStrategies))
            return false;
        if(!externalStrategies.equals(that.externalStrategies))
            return false;
        if(!dynamicRules.equals(that.dynamicRules))
            return false;
        return overlayData.equals(that.overlayData);
    }

    @Override public int hashCode() {
        int result = imports.hashCode();
        result = 31 * result + constructors.hashCode();
        result = 31 * result + injections.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + strategies.hashCode();
        result = 31 * result + internalStrategies.hashCode();
        result = 31 * result + externalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + overlayData.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "ModuleIndex(" + imports + ", " + constructors + ", " + injections + ", "
            + externalConstructors + ", " + strategies + ", " + internalStrategies + ", "
            + externalStrategies + ", " + dynamicRules + ", " + overlayData + ", " + lastModified
            + ')';
    }

    @Override public long lastModified() {
        return lastModified;
    }
}
