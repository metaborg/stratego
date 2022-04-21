package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;

public class OverlayData implements Serializable {
    public final ArrayList<ConstructorData> constrData;
    public final LinkedHashSet<ConstructorSignature> usedConstructors;

    public OverlayData(ArrayList<ConstructorData> overlayData, LinkedHashSet<ConstructorSignature> usedConstructors) {
        this.constrData = overlayData;
        this.usedConstructors = usedConstructors;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + constrData.hashCode();
        result = prime * result + usedConstructors.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        OverlayData other = (OverlayData) obj;
        if(!constrData.equals(other.constrData))
            return false;
        if(!usedConstructors.equals(other.usedConstructors))
            return false;
        return true;
    }

    @Override public String toString() {
        return "OverlayData("
            + "overlayData=" + constrData
            + ", usedConstructors=" + usedConstructors
            + ")";
    }
}
