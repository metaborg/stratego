package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class StringQuotationInMatchTerm extends Message {
    public StringQuotationInMatchTerm(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "The string quotation pattern may not be used in match context.";
    }
}
