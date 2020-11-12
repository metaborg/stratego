package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class DuplicateTypeDefinition extends StrategoMessage {
    public DuplicateTypeDefinition(String module, IStrategoTerm callDynTerm, MessageSeverity severity) {
        super(module, callDynTerm, severity);
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Duplicate type definition.";
    }
}
