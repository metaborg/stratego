package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class WithClauseInDynRule extends Message {
    public WithClauseInDynRule(IStrategoTerm locationTerm, long lastModified) {
        super(locationTerm, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Cannot use with clauses in dynamic rules. ";
    }
}
