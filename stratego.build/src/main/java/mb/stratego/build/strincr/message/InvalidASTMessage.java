package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class InvalidASTMessage extends Message {
    private final String expectedTermDescription;

    public InvalidASTMessage(IStrategoTerm locationTerm, long lastModified, String expectedTermDescription) {
        super(locationTerm, MessageSeverity.ERROR, lastModified);
        this.expectedTermDescription = expectedTermDescription;
    }

    @Override public String getMessage() {
        return "Did not recognise AST shape. Expected " + expectedTermDescription + ".";
    }
}
