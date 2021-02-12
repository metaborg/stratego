package mb.stratego.build.strincr.message;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.MessageSeverity;

public class CyclicOverlay2 extends Message2<IStrategoTerm> {
    public final Set<ConstructorSignatureMatcher> cycle;

    public CyclicOverlay2(IStrategoTerm name, Set<ConstructorSignatureMatcher> cycle, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}
