package mb.stratego.build.strincr.message;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;

public class CyclicOverlay extends Message<IStrategoTerm> {
    public final Set<ConstructorSignatureMatcher> cycle;

    public CyclicOverlay(IStrategoTerm name, Set<ConstructorSignatureMatcher> cycle, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}
