package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;

import mb.pie.api.Supplier;

public class CLCFOutput implements Serializable {
    public final ArrayList<Supplier<?>> unarchiveTasks;

    public CLCFOutput(ArrayList<Supplier<?>> originTasks) {
        this.unarchiveTasks = originTasks;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CLCFOutput that = (CLCFOutput) o;

        return unarchiveTasks.equals(that.unarchiveTasks);
    }

    @Override public int hashCode() {
        return unarchiveTasks.hashCode();
    }

    @Override public String toString() {
        //@formatter:off
        return "CLCFOutput@" + System.identityHashCode(this) + '{'
            + "unarchiveTasks=" + unarchiveTasks.size()
            + '}';
        //@formatter:on
    }
}
