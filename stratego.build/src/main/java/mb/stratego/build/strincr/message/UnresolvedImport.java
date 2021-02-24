package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedImport extends Message {
    public UnresolvedImport(IStrategoTerm importTerm, long lastModified) {
        super(importTerm, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Cannot find module for import '" + locationTerm.toString(1) + "'";
    }
}
