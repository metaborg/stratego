package mb.stratego.build.strincr.function;

import java.util.LinkedHashSet;

import mb.pie.api.SerializableFunction;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.task.output.BackOutput;

public class GetUnreportedResultFiles implements SerializableFunction<BackOutput, LinkedHashSet<ResourcePath>> {
    public static final GetUnreportedResultFiles INSTANCE = new GetUnreportedResultFiles();


    @Override public LinkedHashSet<ResourcePath> apply(BackOutput backOutput) {
        return backOutput.unreportedResultFiles;
    }

    @Override public boolean equals(Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
