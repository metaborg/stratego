package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.StrategySignature;

public class GlobalConsInj implements Serializable {
    public final HashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers;
    public final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections;
    public final HashSet<StrategySignature> nonExternalStrategies;

    public GlobalConsInj(HashSet<IModuleImportService.ModuleIdentifier> allModuleIdentifiers,
        HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> nonExternalInjections,
        HashSet<StrategySignature> nonExternalStrategies) {
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
