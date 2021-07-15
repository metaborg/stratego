package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.data.StrategySignature;

public class CompileGlobalIndex implements Serializable {
    public final ArrayList<Stratego2LibInfo> importedStr2LibProjects;
    public final LinkedHashSet<StrategySignature> nonExternalStrategies;
    public final LinkedHashSet<StrategySignature> dynamicRules;

    public CompileGlobalIndex(ArrayList<Stratego2LibInfo> importedStr2LibProjects, LinkedHashSet<StrategySignature> nonExternalStrategies,
        LinkedHashSet<StrategySignature> dynamicRules) {
        this.importedStr2LibProjects = importedStr2LibProjects;
        this.nonExternalStrategies = nonExternalStrategies;
        this.dynamicRules = dynamicRules;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileGlobalIndex that = (CompileGlobalIndex) o;

        if(!importedStr2LibProjects.equals(that.importedStr2LibProjects))
            return false;
        if(!nonExternalStrategies.equals(that.nonExternalStrategies))
            return false;
        return dynamicRules.equals(that.dynamicRules);
    }

    @Override public int hashCode() {
        int result = importedStr2LibProjects.hashCode();
        result = 31 * result + nonExternalStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        return result;
    }

    @Override public String toString() {
        return "GlobalIndex(" + importedStr2LibProjects + ", " + nonExternalStrategies + ", " + dynamicRules + ')';
    }
}
