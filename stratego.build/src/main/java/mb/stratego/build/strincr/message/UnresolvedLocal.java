package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedLocal extends Message {
    private final IStrategoTerm ID;

    public UnresolvedLocal(IStrategoTerm locationTerm, IStrategoTerm ID, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.ID = ID;
    }

    @Override
    public String getMessage() {
        return "Unresolved local variable " + ID.toString() + ".";
    }
}
