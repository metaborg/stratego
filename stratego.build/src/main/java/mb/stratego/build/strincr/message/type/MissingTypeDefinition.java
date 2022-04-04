package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class MissingTypeDefinition extends TypeMessage<IStrategoTerm> {
    public MissingTypeDefinition(IStrategoTerm subterm, long lastModified) {
        this(subterm, MessageSeverity.NOTE, lastModified);
    }

    public MissingTypeDefinition(IStrategoTerm subterm, MessageSeverity severity, long lastModified) {
        super(subterm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Missing type definition. ";
    }
}
