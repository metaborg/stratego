package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class BuildDefaultInMatchTerm extends Message<IStrategoTerm> {
    public BuildDefaultInMatchTerm(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "The _name pattern may not be used in match context.";
    }
}
