package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class BuildDefaultInBuildTerm extends Message<IStrategoTerm> {
    public BuildDefaultInBuildTerm(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "The _name pattern may not be used in build context.";
    }
}
