package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;

public class GlobalConsInj implements Serializable {
    public final Set<IModuleImportService.ModuleIdentifier> allModuleIdentifiers;
    public final Map<IStrategoTerm, List<IStrategoTerm>> nonExternalInjections;
    public final Collection<StrategySignature> nonExternalStrategies;

    public GlobalConsInj(
        Set<IModuleImportService.ModuleIdentifier> allModuleIdentifiers,
        Map<IStrategoTerm, List<IStrategoTerm>> nonExternalInjections,
        Collection<StrategySignature> nonExternalStrategies) {
        this.allModuleIdentifiers = allModuleIdentifiers;
        this.nonExternalInjections = nonExternalInjections;
        this.nonExternalStrategies = nonExternalStrategies;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        GlobalConsInj that = (GlobalConsInj) o;

        if(!allModuleIdentifiers.equals(that.allModuleIdentifiers))
            return false;
        if(!nonExternalInjections.equals(that.nonExternalInjections))
            return false;
        return nonExternalStrategies.equals(that.nonExternalStrategies);
    }

    @Override public int hashCode() {
        int result = allModuleIdentifiers.hashCode();
        result = 31 * result + nonExternalInjections.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalConsInj(" + allModuleIdentifiers + ", " + nonExternalInjections + ", "
            + nonExternalStrategies + ')';
    }
}
