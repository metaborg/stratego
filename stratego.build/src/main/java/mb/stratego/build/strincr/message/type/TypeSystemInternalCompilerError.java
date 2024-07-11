package mb.stratego.build.strincr.message.type;

import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.message.MessageSeverity;

public class TypeSystemInternalCompilerError extends TypeMessage<IStrategoTerm> {
    public final String message;
    public final List<StrategySignature> defsWithErrT;

    public TypeSystemInternalCompilerError(IStrategoTerm name, String message, List<StrategySignature> defsWithErrT, MessageSeverity severity,
        long lastModified) {
        super(name, severity, lastModified);
        this.message = message;
        this.defsWithErrT = defsWithErrT;
    }

    @Override public String getMessage() {
        return "Internal compiler error: " + message + ": " + defsWithErrT + ". ";
    }
}
