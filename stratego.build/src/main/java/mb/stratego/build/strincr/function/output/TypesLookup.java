package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;

public class TypesLookup implements Serializable {
    public final LinkedHashMap<StrategySignature, StrategyType> strategyTypes;
    public final LinkedHashMap<ConstructorSignature, HashSet<ConstructorType>> constructorTypes;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> allInjections;
    public final ArrayList<IStrategoTerm> imports;
    public final long lastModified;

    public TypesLookup(LinkedHashMap<StrategySignature, StrategyType> strategyTypes,
        LinkedHashMap<ConstructorSignature, HashSet<ConstructorType>> constructorTypes,
        LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> allInjections,
        ArrayList<IStrategoTerm> imports, long lastModified) {
        this.strategyTypes = strategyTypes;
        this.constructorTypes = constructorTypes;
        this.allInjections = allInjections;
        this.imports = imports;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        TypesLookup that = (TypesLookup) o;

        if(lastModified != that.lastModified)
            return false;
        if(!strategyTypes.equals(that.strategyTypes))
            return false;
        if(!constructorTypes.equals(that.constructorTypes))
            return false;
        if(!allInjections.equals(that.allInjections))
            return false;
        return imports.equals(that.imports);
    }

    @Override public int hashCode() {
        int result = strategyTypes.hashCode();
        result = 31 * result + constructorTypes.hashCode();
        result = 31 * result + allInjections.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "TypesLookup(" + strategyTypes + ", " + constructorTypes + ", " + allInjections
            + ", " + imports + ", " + lastModified + ')';
    }
}
