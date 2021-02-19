package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class UnresolvedImport extends JavaMessage<IStrategoTerm> {
    public UnresolvedImport(String module, IStrategoTerm importTerm) {
        super(module, importTerm, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Cannot find module for import '" + locationTerm.toString(1) + "'";
    }
}
