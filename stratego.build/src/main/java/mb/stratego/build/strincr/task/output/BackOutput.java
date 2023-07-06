package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.LinkedHashSet;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;

public class BackOutput implements Serializable {
    public final LinkedHashSet<ResourcePath> resultFiles;
    public final LinkedHashSet<ResourcePath> unreportedResultFiles;
    public final LinkedHashSet<? extends StrategySignature> compiledStrategies;
    public final boolean depTasksHaveErrorMessages;
    protected final int hashCode;

    public static final BackOutput dependentTasksHaveErrorMessages =
        new BackOutput(new LinkedHashSet<>(0), new LinkedHashSet<>(0), new LinkedHashSet<>(0),
            true);

    public BackOutput(LinkedHashSet<ResourcePath> resultFiles,
        LinkedHashSet<ResourcePath> unreportedResultFiles,
        LinkedHashSet<? extends StrategySignature> compiledStrategies) {
        this(resultFiles, unreportedResultFiles, compiledStrategies, false);
    }

    private BackOutput(LinkedHashSet<ResourcePath> resultFiles,
        LinkedHashSet<ResourcePath> unreportedResultFiles,
        LinkedHashSet<? extends StrategySignature> compiledStrategies,
        boolean depTasksHaveErrorMessages) {
        this.resultFiles = resultFiles;
        this.unreportedResultFiles = unreportedResultFiles;
        this.compiledStrategies = compiledStrategies;
        this.depTasksHaveErrorMessages = depTasksHaveErrorMessages;
        this.hashCode = hashFunction();
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        BackOutput that = (BackOutput) o;

        if(hashCode != that.hashCode)
            return false;
        if(depTasksHaveErrorMessages != that.depTasksHaveErrorMessages)
            return false;
        if(!resultFiles.equals(that.resultFiles))
            return false;
        return compiledStrategies.equals(that.compiledStrategies);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = resultFiles.hashCode();
        result = 31 * result + compiledStrategies.hashCode();
        result = 31 * result + (depTasksHaveErrorMessages ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "BackOutput@" + System.identityHashCode(this) + '{'
            + "resultFiles=" + resultFiles.size()
            + ", unreportedResultFiles=" + unreportedResultFiles.size()
            + ", compiledStrategies=" + compiledStrategies.size()
            + ", depTasksHaveErrorMessages=" + depTasksHaveErrorMessages
            + '}';
        //@formatter:on
    }
}
