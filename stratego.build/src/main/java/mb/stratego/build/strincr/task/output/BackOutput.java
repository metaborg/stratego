package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.HashSet;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;

public class BackOutput implements Serializable {
    public final HashSet<ResourcePath> resultFiles;
    public final HashSet<? extends StrategySignature> compiledStrategies;

    public BackOutput(HashSet<ResourcePath> resultFiles,
        HashSet<? extends StrategySignature> compiledStrategies) {
        this.resultFiles = resultFiles;
        this.compiledStrategies = compiledStrategies;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        BackOutput output = (BackOutput) o;

        if(!resultFiles.equals(output.resultFiles))
            return false;
        return compiledStrategies.equals(output.compiledStrategies);
    }

    @Override public int hashCode() {
        int result = resultFiles.hashCode();
        result = 31 * result + compiledStrategies.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Back.Output(" + resultFiles + ")";
    }
}
