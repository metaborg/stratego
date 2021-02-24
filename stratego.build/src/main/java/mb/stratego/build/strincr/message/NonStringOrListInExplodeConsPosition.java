package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class NonStringOrListInExplodeConsPosition extends Message {
    public final IStrategoTerm type;

    public NonStringOrListInExplodeConsPosition(IStrategoTerm locationTerm, IStrategoTerm type,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.type = type;
    }

    @Override
    public String getMessage() {
        return "Expected string or List, but got " + type + ".";
    }
}
