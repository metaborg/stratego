package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class ExternalStrategyNotFound extends JavaMessage<IStrategoString> {
    public ExternalStrategyNotFound(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Cannot find external strategy or rule '" + locationTerm.stringValue() + "'";
    }
}
