package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class AsInBuildTerm extends Message<IStrategoTerm> {
    public AsInBuildTerm(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "The @ pattern may not be used in build context.";
    }
}
