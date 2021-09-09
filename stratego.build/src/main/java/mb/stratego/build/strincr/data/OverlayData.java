package mb.stratego.build.strincr.data;

import java.util.LinkedHashSet;

import org.spoofax.interpreter.terms.IStrategoAppl;

public class OverlayData extends ConstructorData {
    public final LinkedHashSet<ConstructorSignature> usedConstructors;

    public OverlayData(ConstructorSignature signature, IStrategoAppl astTerm, ConstructorType type,
        LinkedHashSet<ConstructorSignature> usedConstructors) {
        super(signature, astTerm, type);
        this.usedConstructors = usedConstructors;
    }

    @Override public boolean isOverlay() {
        return true;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        OverlayData that = (OverlayData) o;

        return usedConstructors.equals(that.usedConstructors);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + usedConstructors.hashCode();
        return result;
    }

    @Override public String toString() {
        return "OverlayData(" + signature + ", " + astTerm + ", " + type
            + ", " + usedConstructors + ')';
    }
}
