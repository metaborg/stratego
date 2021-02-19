package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class NonStringOrListInExplodeConsPosition extends StrategoMessage {
    public final IStrategoTerm type;

    public NonStringOrListInExplodeConsPosition(String module, IStrategoTerm locationTerm, IStrategoTerm type,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.type = type;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Expected string or List, but got " + type + ".";
    }
}
