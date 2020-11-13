package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class TypeMismatch extends StrategoMessage {
    public final IStrategoTerm expected;
    public final IStrategoTerm actual;

    public TypeMismatch(String module, IStrategoTerm locationTerm, IStrategoTerm expected, IStrategoTerm actual,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.expected = expected;
        this.actual = actual;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Expected " + expected + ", but got " + actual + ".";
    }
}
