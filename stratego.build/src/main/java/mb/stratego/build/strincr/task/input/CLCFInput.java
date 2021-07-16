package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.Stratego2LibInfo;

public class CLCFInput implements Serializable {
    public final Stratego2LibInfo stratego2LibInfo;
    public final ResourcePath outputDir;

    public CLCFInput(Stratego2LibInfo stratego2LibInfo, ResourcePath outputDir) {
        this.stratego2LibInfo = stratego2LibInfo;
        this.outputDir = outputDir;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CLCFInput clcfInput = (CLCFInput) o;

        return stratego2LibInfo.equals(clcfInput.stratego2LibInfo);
    }

    @Override public int hashCode() {
        return stratego2LibInfo.hashCode();
    }

    @Override public String toString() {
        return "CLCFInput(" + stratego2LibInfo + ')';
    }
}
