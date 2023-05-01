package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;

public class TypesLookup implements Serializable {
    public final LinkedHashMap<StrategySignature, StrategyType> strategyTypes;
    public final LinkedHashMap<ConstructorSignature, HashSet<ConstructorType>> constructorTypes;
    public final LinkedHashSet<SortSignature> sorts;
    public final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> allInjections;
    public final ArrayList<IModuleImportService.ModuleIdentifier> imports;
    public final long lastModified;

    public TypesLookup(LinkedHashMap<StrategySignature, StrategyType> strategyTypes,
        LinkedHashMap<ConstructorSignature, HashSet<ConstructorType>> constructorTypes,
        LinkedHashSet<SortSignature> sorts, LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> allInjections,
        ArrayList<IModuleImportService.ModuleIdentifier> imports, long lastModified) {
        this.strategyTypes = strategyTypes;
        this.constructorTypes = constructorTypes;
        this.sorts = sorts;
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
        if(!sorts.equals(that.sorts))
            return false;
        if(!imports.equals(that.imports))
            return false;
        return allInjections.equals(that.allInjections);
    }

    @Override public int hashCode() {
        int result = strategyTypes.hashCode();
        result = 31 * result + constructorTypes.hashCode();
        result = 31 * result + sorts.hashCode();
        result = 31 * result + allInjections.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }

    @Override public String toString() {
        return "TypesLookup(" + strategyTypes + ", " + constructorTypes + ", " + sorts + ", " + allInjections
            + ", " + imports + ", " + lastModified + ')';
    }
}
