package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class CallStrategyArgumentTakesParameters extends Message<IStrategoTerm> {
    public final IStrategoTerm sfuntype;

    public CallStrategyArgumentTakesParameters(IStrategoTerm locationTerm, IStrategoTerm sfuntype,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.sfuntype = sfuntype;
    }

    @Override
    public String getMessage() {
        return "This call takes parameters, it has type: " + sfuntype;
    }
}
