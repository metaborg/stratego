package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class StringQuotationInOverlay extends Message {
    public StringQuotationInOverlay(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "The string quotation pattern may not be used in overlay.";
    }
}
