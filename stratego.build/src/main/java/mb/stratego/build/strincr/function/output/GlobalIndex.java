package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.StrategySignature;

public class GlobalIndex implements Serializable {
    public final LinkedHashSet<ConstructorSignature> nonExternalConstructors;
    public final LinkedHashSet<ConstructorSignature> externalConstructors;
    public final LinkedHashSet<StrategySignature> nonExternalStrategies;
    public final LinkedHashSet<StrategySignature> dynamicRules;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections;

    public GlobalIndex(LinkedHashSet<ConstructorSignature> nonExternalConstructors,
        LinkedHashSet<ConstructorSignature> externalConstructors,
        LinkedHashSet<StrategySignature> nonExternalStrategies,
        LinkedHashSet<StrategySignature> dynamicRules,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections) {
        this.nonExternalConstructors = nonExternalConstructors;
        this.externalConstructors = externalConstructors;
        this.nonExternalStrategies = nonExternalStrategies;
        this.dynamicRules = dynamicRules;
        this.nonExternalInjections = nonExternalInjections;
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
        if(!nonExternalInjections.equals(that.nonExternalInjections))
            return false;
        return dynamicRules.equals(that.dynamicRules);
    }

    @Override public int hashCode() {
        int result = nonExternalConstructors.hashCode();
        result = 31 * result + externalConstructors.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        result = 31 * result + nonExternalInjections.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalIndex(" + nonExternalConstructors + ", " + externalConstructors + ", "
            + nonExternalStrategies + ", " + dynamicRules + ", " + nonExternalInjections + ')';
    }
}