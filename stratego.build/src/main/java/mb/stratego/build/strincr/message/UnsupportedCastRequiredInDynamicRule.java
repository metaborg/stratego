package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnsupportedCastRequiredInDynamicRule extends Message {
    public UnsupportedCastRequiredInDynamicRule(IStrategoTerm locationTerm,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Pattern induces cast, but cast is not supported in this position.";
    }
}
