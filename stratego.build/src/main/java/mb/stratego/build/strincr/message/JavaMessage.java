package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;

public abstract class JavaMessage<T extends IStrategoTerm> extends Message<T> {
    public JavaMessage(String module, T locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }
}