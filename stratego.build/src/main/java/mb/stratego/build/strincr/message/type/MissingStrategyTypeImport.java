package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class MissingStrategyTypeImport extends TypeMessage<IStrategoTerm> {
    public MissingStrategyTypeImport(IStrategoTerm subterm, long lastModified) {
        super(subterm, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Extends typed definition, but does not import the type. ";
    }
}
