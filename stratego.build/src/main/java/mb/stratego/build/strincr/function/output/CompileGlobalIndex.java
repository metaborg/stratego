package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import mb.stratego.build.strincr.data.StrategySignature;

public class CompileGlobalIndex implements Serializable {
    public final ArrayList<String> importedStr2LibPackageNames;
    public final LinkedHashSet<StrategySignature> nonExternalStrategies;
    public final TreeSet<StrategySignature> dynamicRules;

    public CompileGlobalIndex(ArrayList<String> importedStr2LibPackageNames, LinkedHashSet<StrategySignature> nonExternalStrategies,
        TreeSet<StrategySignature> dynamicRules) {
        this.importedStr2LibPackageNames = importedStr2LibPackageNames;
        this.nonExternalStrategies = nonExternalStrategies;
        this.dynamicRules = dynamicRules;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileGlobalIndex that = (CompileGlobalIndex) o;

        if(!importedStr2LibPackageNames.equals(that.importedStr2LibPackageNames))
            return false;
        if(!nonExternalStrategies.equals(that.nonExternalStrategies))
            return false;
        return dynamicRules.equals(that.dynamicRules);
    }

    @Override public int hashCode() {
        int result = importedStr2LibPackageNames.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalIndex(" + importedStr2LibPackageNames + ", " + nonExternalStrategies + ", " + dynamicRules + ')';
    }
}
