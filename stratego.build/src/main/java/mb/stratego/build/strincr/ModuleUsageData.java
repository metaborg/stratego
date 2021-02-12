package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.util.WithLastModified;

public class ModuleUsageData implements Serializable, WithLastModified {
    public final IStrategoTerm ast;
    public final List<IStrategoTerm> imports;
    public final Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData;
    public final Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData;
    public final Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData;
    public final Set<ConstructorSignature> usedConstructors;
    public final Set<StrategySignature> usedStrategies;
    public final Set<String> usedAmbiguousStrategies;
    public final long lastModified;

    public ModuleUsageData(IStrategoTerm ast, List<IStrategoTerm> imports,
        Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData,
        Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData,
        Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData, Set<ConstructorSignature> usedConstructors,
        Set<StrategySignature> usedStrategies, Set<String> usedAmbiguousStrategies, long lastModified) {
        this.ast = ast;
        this.imports = imports;
        this.normalStrategyData = normalStrategyData;
        this.internalStrategyData = internalStrategyData;
        this.externalStrategyData = externalStrategyData;
        this.usedConstructors = usedConstructors;
        this.usedStrategies = usedStrategies;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.lastModified = lastModified;
    }

    @Override public long lastModified() {
        return lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleUsageData that = (ModuleUsageData) o;

        if(lastModified != that.lastModified)
            return false;
        if(!ast.equals(that.ast))
            return false;
        if(!imports.equals(that.imports))
            return false;
        if(!normalStrategyData.equals(that.normalStrategyData))
            return false;
        if(!internalStrategyData.equals(that.internalStrategyData))
            return false;
        if(!externalStrategyData.equals(that.externalStrategyData))
            return false;
        if(!usedConstructors.equals(that.usedConstructors))
            return false;
        if(!usedStrategies.equals(that.usedStrategies))
            return false;
        return usedAmbiguousStrategies.equals(that.usedAmbiguousStrategies);
    }

    @Override public int hashCode() {
        int result = ast.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + normalStrategyData.hashCode();
        result = 31 * result + internalStrategyData.hashCode();
        result = 31 * result + externalStrategyData.hashCode();
        result = 31 * result + usedConstructors.hashCode();
        result = 31 * result + usedStrategies.hashCode();
        result = 31 * result + usedAmbiguousStrategies.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }
}
