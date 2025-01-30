package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedSortVar extends Message {

    private final String sortVar;

    public UnresolvedSortVar(IStrategoTerm locationTerm, String sortVar, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.sortVar = sortVar;
    }

    @Override
    public String getMessage() {
        return "Undefined sort variable " + sortVar + ".";
    }
}
