package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class MatchNotSpecificEnoughForTP extends StrategoMessage {
    public final IStrategoTerm type;

    public MatchNotSpecificEnoughForTP(String module, IStrategoTerm locationTerm, IStrategoTerm type,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.type = type;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Cannot infer specific type for TP rule match. Found result: " + type;
    }
}
