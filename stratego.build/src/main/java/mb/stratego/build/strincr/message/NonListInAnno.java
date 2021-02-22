package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class NonListInAnno extends Message<IStrategoTerm> {
    public final IStrategoTerm type;

    public NonListInAnno(IStrategoTerm locationTerm, IStrategoTerm type, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.type = type;
    }

    @Override
    public String getMessage() {
        return "Expected List, but got " + type;
    }
}
