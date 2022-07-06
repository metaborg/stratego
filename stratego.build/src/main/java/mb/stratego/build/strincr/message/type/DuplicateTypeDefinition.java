package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class DuplicateTypeDefinition extends TypeMessage<IStrategoTerm> {
    public DuplicateTypeDefinition(IStrategoTerm locationTerm, long lastModified) {
        this(locationTerm, MessageSeverity.ERROR, lastModified);
    }

    public DuplicateTypeDefinition(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Duplicate type definition.";
    }
}
