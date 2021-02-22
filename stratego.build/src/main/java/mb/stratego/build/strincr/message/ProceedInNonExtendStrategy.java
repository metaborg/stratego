package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class ProceedInNonExtendStrategy extends Message<IStrategoTerm> {
    public ProceedInNonExtendStrategy(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "Unexpected call to proceed in strategy that does not extend an external strategy. ";
    }
}
