package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class DuplicateTypeDefinition extends StrategoMessage {
    public DuplicateTypeDefinition(String module, IStrategoTerm locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Duplicate type definition.";
    }
}
