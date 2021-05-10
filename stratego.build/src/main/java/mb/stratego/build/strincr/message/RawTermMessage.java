package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class RawTermMessage extends Message {
    private final IStrategoTerm messageTerm;

    public RawTermMessage(IStrategoTerm locationTerm, IStrategoTerm messageTerm,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.messageTerm = messageTerm;
    }

    @Override
    public String getMessage() {
        return this.messageTerm.toString();
    }
}
