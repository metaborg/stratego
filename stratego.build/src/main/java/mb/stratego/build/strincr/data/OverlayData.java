package mb.stratego.build.strincr.data;

import java.util.HashSet;

import org.spoofax.interpreter.terms.IStrategoAppl;

public class OverlayData extends ConstructorData {
    public final HashSet<ConstructorSignature> usedConstructors;

    public OverlayData(ConstructorSignature signature, IStrategoAppl astTerm, ConstructorType type,
        HashSet<ConstructorSignature> usedConstructors) {
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
