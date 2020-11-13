package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class UnresolvedWildcardImport extends JavaMessage<IStrategoString> {
    public UnresolvedWildcardImport(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Cannot find directory for wildcard import '" + locationTerm.stringValue() + "'";
    }
}
