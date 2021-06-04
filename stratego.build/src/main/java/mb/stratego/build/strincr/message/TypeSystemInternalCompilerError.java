package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class TypeSystemInternalCompilerError extends Message {
    public TypeSystemInternalCompilerError(IStrategoTerm name, MessageSeverity severity,
        long lastModified) {
        super(name, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Internal compiler error: type system did not give errors but did insert cast to error type. ";
    }
}
