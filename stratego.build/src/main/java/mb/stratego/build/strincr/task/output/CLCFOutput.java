package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;

import mb.resource.hierarchical.ResourcePath;

public class CLCFOutput implements Serializable {
    public final ArrayList<ResourcePath> writtenClassFiles;
    protected final int hashCode;

    public CLCFOutput(ArrayList<ResourcePath> writtenClassFiles) {
        this.writtenClassFiles = writtenClassFiles;
        this.hashCode = hashFunction();
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CLCFOutput that = (CLCFOutput) o;

        if(hashCode != that.hashCode)
            return false;
        return writtenClassFiles.equals(that.writtenClassFiles);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
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
