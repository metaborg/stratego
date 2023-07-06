package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class WithClauseInDynRule extends Message {
    public WithClauseInDynRule(IStrategoTerm locationTerm, long lastModified) {
        this(locationTerm, MessageSeverity.ERROR, lastModified);
    }

    public WithClauseInDynRule(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Cannot use with clauses in dynamic rules. ";
    }
}
