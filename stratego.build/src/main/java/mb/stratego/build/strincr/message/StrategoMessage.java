package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;

public abstract class StrategoMessage extends Message<IStrategoTerm> {
    public StrategoMessage(String module, IStrategoTerm locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }

    @Override
    public String getMessage() {
        return getMessageWithoutLocation() + " @ " + locationTerm.toString();
    }

    public abstract String getMessageWithoutLocation();
}
