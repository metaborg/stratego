package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class NoInjectionBetween extends StrategoMessage {
    public final IStrategoTerm from;
    public final IStrategoTerm to;

    public NoInjectionBetween(String module, IStrategoTerm locationTerm, IStrategoTerm from, IStrategoTerm to,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.from = from;
        this.to = to;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Cannot convert from " + from + " to " + to + " automatically, no known injection.";
    }
}
