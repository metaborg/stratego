package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;

public abstract class TypeMessage<T extends IStrategoTerm> extends Message {
    public TypeMessage(T name, MessageSeverity severity, long lastModified) {
        super(name, severity, lastModified);
    }
}
