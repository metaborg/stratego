package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.StrategoMessage;

public class UnresolvedLocal extends StrategoMessage {
    public UnresolvedLocal(String module, IStrategoTerm locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Unresolved local variable.";
    }
}
