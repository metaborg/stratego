package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UsingStratego1File extends Message {
    public UsingStratego1File(IStrategoTerm locationTerm, long lastModified) {
        super(locationTerm, MessageSeverity.WARNING, lastModified);
    }

    @Override public String getMessage() {
        return "Using str file, no str2 file found for: " + locationTermString;
    }
}
