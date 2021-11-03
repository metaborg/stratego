package mb.stratego.build.strincr.message;

import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.data.StrategySignature;

public class TypeSystemInternalCompilerError extends Message {
    public final List<StrategySignature> defsWithErrT;

    public TypeSystemInternalCompilerError(IStrategoTerm name, List<StrategySignature> defsWithErrT, MessageSeverity severity,
        long lastModified) {
        super(name, severity, lastModified);
        this.defsWithErrT = defsWithErrT;
    }

    @Override public String getMessage() {
        return "Internal compiler error: type system did not give errors but did insert cast to error type in: " + defsWithErrT + ". ";
    }
}
