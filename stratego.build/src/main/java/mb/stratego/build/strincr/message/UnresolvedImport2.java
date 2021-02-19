package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedImport2 extends Message2<IStrategoTerm> {
    public UnresolvedImport2(IStrategoTerm importTerm, long lastModified) {
        super(importTerm, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Cannot find module for import '" + locationTerm.toString(1) + "'";
    }
}
