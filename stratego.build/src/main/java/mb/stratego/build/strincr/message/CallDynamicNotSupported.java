package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class CallDynamicNotSupported extends Message<IStrategoTerm> {
    public CallDynamicNotSupported(IStrategoTerm callDynTerm, MessageSeverity severity, long lastModified) {
        super(callDynTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "The dynamic call construct is no longer supported.";
    }
}
