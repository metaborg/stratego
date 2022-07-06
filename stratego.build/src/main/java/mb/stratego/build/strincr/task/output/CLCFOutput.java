package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;

import mb.resource.hierarchical.ResourcePath;

public class CLCFOutput implements Serializable {
    public final ArrayList<ResourcePath> writtenClassFiles;

    public CLCFOutput(ArrayList<ResourcePath> writtenClassFiles) {
        this.writtenClassFiles = writtenClassFiles;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CLCFOutput that = (CLCFOutput) o;

        return writtenClassFiles.equals(that.writtenClassFiles);
    }

    @Override public int hashCode() {
        return writtenClassFiles.hashCode();
    }

    @Override public String toString() {
        //@formatter:off
        return "CLCFOutput@" + System.identityHashCode(this) + '{'
            + "writtenClassFiles=" + writtenClassFiles.size()
            + '}';
        //@formatter:on
    }
}
