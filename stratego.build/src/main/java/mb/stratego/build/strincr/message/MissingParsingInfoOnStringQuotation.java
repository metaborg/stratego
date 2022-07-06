package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class MissingParsingInfoOnStringQuotation extends Message {
    public MissingParsingInfoOnStringQuotation(IStrategoTerm locationTerm, MessageSeverity messageSeverity, long lastModified) {
        super(locationTerm, messageSeverity, lastModified);
    }

    @Override public String getMessage() {
        return "String quotation does not have parser information required to figure out indentation.";
    }
}
