package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class MissingDefinitionForTypeDefinition extends TypeMessage<IStrategoTerm> {
    public MissingDefinitionForTypeDefinition(IStrategoTerm callDynTerm, MessageSeverity severity,
        long lastModified) {
        super(callDynTerm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Cannot find definition corresponding to this type definition.";
    }
}
