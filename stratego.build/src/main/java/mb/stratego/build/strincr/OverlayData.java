package mb.stratego.build.strincr;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoAppl;

public class OverlayData extends ConstructorData {
    public final Set<ConstructorSignature> usedConstructors;

    public OverlayData(ConstructorSignature signature, IStrategoAppl astTerm, ConstructorType type,
        Set<ConstructorSignature> usedConstructors) {
        super(signature, astTerm, type);
        this.usedConstructors = usedConstructors;
    }

    @Override public boolean isOverlay() {
        return true;
    }
}
