package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoString;

public class VarConstrOverlap extends Message<IStrategoString> {
    public VarConstrOverlap(IStrategoString name, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Nullary constructor '" + locationTerm.stringValue() + "' should be followed by round brackets ().";
    }
}
