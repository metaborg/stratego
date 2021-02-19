package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class BuildUnboundTerm extends StrategoMessage {
    public BuildUnboundTerm(String module, IStrategoTerm locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Use of unbound local variable.";
    }
}
