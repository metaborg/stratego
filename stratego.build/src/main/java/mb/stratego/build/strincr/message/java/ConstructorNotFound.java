package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class ConstructorNotFound extends JavaMessage<IStrategoString> {
    public ConstructorNotFound(String module, IStrategoString name, MessageSeverity severity) {
        super(module, name, severity);
    }

    @Override public String getMessage() {
        return "Cannot find constructor '" + locationTerm.stringValue() + "'";
    }
}
