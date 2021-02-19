package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

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
