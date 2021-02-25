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

    @Override public String toString() {
        return "OverlayData(" + signature + ", " + astTerm + ", " + type
            + ", " + usedConstructors + ')';
    }
}
