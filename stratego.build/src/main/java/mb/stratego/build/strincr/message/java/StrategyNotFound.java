package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class StrategyNotFound extends JavaMessage<IStrategoString> {
    public StrategyNotFound(String module, IStrategoString name, MessageSeverity severity) {
        super(module, name, severity);
    }

    @Override public String getMessage() {
        if(severity == MessageSeverity.ERROR) {
            return "Cannot find strategy or rule '" + locationTerm.stringValue() + "'";
        }
        return "Found '" + locationTerm.stringValue() + "' in a compiled library that was not imported directly or indirectly by this module";
    }
}
