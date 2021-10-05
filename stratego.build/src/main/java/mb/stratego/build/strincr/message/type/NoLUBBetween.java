package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class NoLUBBetween extends TypeMessage<IStrategoTerm> {
    public final IStrategoTerm one;
    public final IStrategoTerm other;

    public NoLUBBetween(IStrategoTerm locationTerm, IStrategoTerm one, IStrategoTerm other,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.one = one;
        this.other = other;
    }

    @Override
    public String getMessage() {
        return "Expected " + one + " and " + other + " to have a shared least upper bound but none was found.";
    }
}
