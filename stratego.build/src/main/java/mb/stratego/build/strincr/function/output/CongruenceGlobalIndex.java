package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.StrategySignature;

public class CongruenceGlobalIndex implements Serializable {
    public final LinkedHashSet<ConstructorSignature> nonExternalConstructors;
    public final LinkedHashSet<ConstructorSignature> externalConstructors;
    public final LinkedHashSet<StrategySignature> nonExternalStrategies;
    public final LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayData;

    public CongruenceGlobalIndex(LinkedHashSet<ConstructorSignature> nonExternalConstructors,
        LinkedHashSet<ConstructorSignature> externalConstructors,
        LinkedHashSet<StrategySignature> nonExternalStrategies,
        LinkedHashMap<ConstructorSignature, ArrayList<IStrategoTerm>> overlayData) {
        this.nonExternalConstructors = nonExternalConstructors;
        this.externalConstructors = externalConstructors;
        this.nonExternalStrategies = nonExternalStrategies;
        this.overlayData = overlayData;
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
        if(!nonExternalStrategies.equals(that.nonExternalStrategies))
            return false;
        return overlayData.equals(that.overlayData);
    }

    @Override public int hashCode() {
        int result = nonExternalConstructors.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        result = 31 * result + overlayData.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalIndex(" + nonExternalConstructors + ", " + externalConstructors + ", "
            + nonExternalStrategies + ", " + overlayData + ')';
    }
}
