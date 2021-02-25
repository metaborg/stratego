package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.LinkedHashSet;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;

public class BackOutput implements Serializable {
    public final LinkedHashSet<ResourcePath> resultFiles;
    public final LinkedHashSet<? extends StrategySignature> compiledStrategies;

    public BackOutput(LinkedHashSet<ResourcePath> resultFiles,
        LinkedHashSet<? extends StrategySignature> compiledStrategies) {
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
