package mb.stratego.build.strincr.message;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignature;

public class CyclicOverlay extends Message {
    public final Set<ConstructorSignature> cycle;

    public CyclicOverlay(IStrategoTerm name, Set<ConstructorSignature> cycle, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}
