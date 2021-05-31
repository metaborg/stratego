package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class TypeMismatch extends TypeMessage<IStrategoTerm> {
    public final IStrategoTerm expected;
    public final IStrategoTerm actual;

    public TypeMismatch(IStrategoTerm locationTerm, IStrategoTerm expected, IStrategoTerm actual,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.expected = expected;
        this.actual = actual;
    }

    @Override
    public String getMessage() {
        return "Expected " + expected + ", but got " + actual + ".";
    }
}
