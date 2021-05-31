package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class NoInjectionBetween extends TypeMessage<IStrategoTerm> {
    public final IStrategoTerm from;
    public final IStrategoTerm to;

    public NoInjectionBetween(IStrategoTerm locationTerm, IStrategoTerm from, IStrategoTerm to,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.from = from;
        this.to = to;
    }

    @Override
    public String getMessage() {
        return "Cannot convert from " + from + " to " + to + " automatically, no known injection.";
    }
}
