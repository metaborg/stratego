package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class MultipleAppsInMatch extends Message {
    public MultipleAppsInMatch(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "Multiple projections in one pattern, only one is actually returned.";
    }
}
