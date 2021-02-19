package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class RawTermMessage extends StrategoMessage {
    private final IStrategoTerm messageTerm;

    public RawTermMessage(String module, IStrategoTerm locationTerm, IStrategoTerm messageTerm,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.messageTerm = messageTerm;
    }

    @Override
    public String getMessageWithoutLocation() {
        return this.messageTerm.toString();
    }
}
