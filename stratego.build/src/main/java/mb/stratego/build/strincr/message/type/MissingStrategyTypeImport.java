package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class MissingStrategyTypeImport extends TypeMessage<IStrategoTerm> {
    public MissingStrategyTypeImport(IStrategoTerm subterm, long lastModified) {
        this(subterm, MessageSeverity.ERROR, lastModified);
    }

    public MissingStrategyTypeImport(IStrategoTerm subterm, MessageSeverity severity, long lastModified) {
        super(subterm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Extends typed definition, but does not import the type. ";
    }
}
