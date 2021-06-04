package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.LinkedHashSet;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;

public class BackOutput implements Serializable {
    public final LinkedHashSet<ResourcePath> resultFiles;
    public final LinkedHashSet<? extends StrategySignature> compiledStrategies;
    public final boolean depTasksHaveErrorMessages;

    public static final BackOutput dependentTasksHaveErrorMessages =
        new BackOutput(new LinkedHashSet<>(0), new LinkedHashSet<>(0), true);

    public BackOutput(LinkedHashSet<ResourcePath> resultFiles,
        LinkedHashSet<? extends StrategySignature> compiledStrategies) {
        this(resultFiles, compiledStrategies, false);
    }

    private BackOutput(LinkedHashSet<ResourcePath> resultFiles,
        LinkedHashSet<? extends StrategySignature> compiledStrategies,
        boolean depTasksHaveErrorMessages) {
        this.resultFiles = resultFiles;
        this.compiledStrategies = compiledStrategies;
        this.depTasksHaveErrorMessages = depTasksHaveErrorMessages;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        BackOutput that = (BackOutput) o;

        if(depTasksHaveErrorMessages != that.depTasksHaveErrorMessages)
            return false;
        if(!resultFiles.equals(that.resultFiles))
            return false;
        return compiledStrategies.equals(that.compiledStrategies);
    }

    @Override public int hashCode() {
        int result = resultFiles.hashCode();
        result = 31 * result + compiledStrategies.hashCode();
        result = 31 * result + (depTasksHaveErrorMessages ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "Back.Output(" + resultFiles + ")";
    }
}
