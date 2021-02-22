package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class MatchNotSpecificEnoughForTP extends TypeMessage<IStrategoTerm> {
    public final IStrategoTerm type;

    public MatchNotSpecificEnoughForTP(IStrategoTerm locationTerm, IStrategoTerm type,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.type = type;
    }

    @Override
    public String getMessage() {
        return "Cannot infer specific type for TP rule match. Found result: " + type;
    }
}
