package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class ConstantCongruence extends Message {
    public ConstantCongruence(IStrategoTerm congruence, long lastModified) {
        this(congruence, MessageSeverity.WARNING, lastModified);
    }

    public ConstantCongruence(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Simple matching congruence: prefix with '?'. Or with '!' if you meant to build.";
    }
}
