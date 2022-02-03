package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class DuplicateTypeDefinition extends TypeMessage<IStrategoTerm> {
    public DuplicateTypeDefinition(IStrategoTerm locationTerm, long lastModified) {
        super(locationTerm, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Duplicate type definition.";
    }
}
