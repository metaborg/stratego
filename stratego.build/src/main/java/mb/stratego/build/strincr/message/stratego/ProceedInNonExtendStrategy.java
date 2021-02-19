package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class ProceedInNonExtendStrategy extends StrategoMessage {
    public ProceedInNonExtendStrategy(String module, IStrategoTerm locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Unexpected call to proceed in strategy that does not extend an external strategy. ";
    }
}
