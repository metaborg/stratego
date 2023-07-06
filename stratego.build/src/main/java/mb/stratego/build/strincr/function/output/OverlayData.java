package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;

public class OverlayData implements Serializable {
    public final ArrayList<IStrategoTerm> constrAsts;
    public final LinkedHashSet<ConstructorSignature> usedConstructors;

    public OverlayData(ArrayList<IStrategoTerm> constrAsts, LinkedHashSet<ConstructorSignature> usedConstructors) {
        this.constrAsts = constrAsts;
        this.usedConstructors = usedConstructors;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + constrAsts.hashCode();
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
        if(!constrAsts.equals(other.constrAsts))
            return false;
        if(!usedConstructors.equals(other.usedConstructors))
            return false;
        return true;
    }

    @Override public String toString() {
        return "OverlayData("
            + "overlayData=" + constrAsts
            + ", usedConstructors=" + usedConstructors
            + ")";
    }
}
