package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class NonListInAnno extends StrategoMessage {
    public final IStrategoTerm type;

    public NonListInAnno(String module, IStrategoTerm locationTerm, IStrategoTerm type, MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.type = type;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Expected List, but got " + type;
    }
}
