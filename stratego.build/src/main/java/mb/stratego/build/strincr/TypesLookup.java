package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class TypesLookup implements Serializable {
    public final Map<StrategySignature, StrategyType> strategyTypes;
    public final Map<ConstructorSignature, Set<ConstructorType>> constructorTypes;
    public final Map<IStrategoTerm, List<IStrategoTerm>> injections;
    public final List<IStrategoTerm> imports;
    public final long lastModified;

    public TypesLookup(Map<StrategySignature, StrategyType> strategyTypes,
        Map<ConstructorSignature, Set<ConstructorType>> constructorTypes,
        Map<IStrategoTerm, List<IStrategoTerm>> injections, List<IStrategoTerm> imports,
        long lastModified) {
        this.strategyTypes = strategyTypes;
        this.constructorTypes = constructorTypes;
        this.injections = injections;
        this.imports = imports;
        this.lastModified = lastModified;
    }
}
