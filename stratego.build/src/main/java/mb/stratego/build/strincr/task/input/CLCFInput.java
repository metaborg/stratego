package mb.stratego.build.strincr.task.input;

import java.io.Serializable;

import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.Stratego2LibInfo;

public class CLCFInput implements Serializable {
    public final Supplier<Stratego2LibInfo> stratego2LibInfoSupplier;
    public final ResourcePath outputDir;

    public CLCFInput(Supplier<Stratego2LibInfo> stratego2LibInfoSupplier, ResourcePath outputDir) {
        this.stratego2LibInfoSupplier = stratego2LibInfoSupplier;
        this.outputDir = outputDir;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CLCFInput clcfInput = (CLCFInput) o;

        if(!stratego2LibInfoSupplier.equals(clcfInput.stratego2LibInfoSupplier))
            return false;
        return outputDir.equals(clcfInput.outputDir);
    }

    @Override public int hashCode() {
        int result = stratego2LibInfoSupplier.hashCode();
        result = 31 * result + outputDir.hashCode();
        return result;
    }

    @Override public String toString() {
        return "CLCFInput(" + stratego2LibInfoSupplier + ", " + outputDir + ')';
    }
}
