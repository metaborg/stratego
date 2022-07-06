package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class WldInOverlay extends Message {
    public WldInOverlay(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "Wildcard not allowed in overlay, add a default value after the underscore.";
    }
}
