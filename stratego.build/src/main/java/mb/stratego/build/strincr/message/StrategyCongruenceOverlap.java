package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class StrategyCongruenceOverlap extends Message {
    public StrategyCongruenceOverlap(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "Strategy overlaps with congruence for constructor of same name.";
    }
}
