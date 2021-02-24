package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedStrategy extends Message {
    public final int strategyArity;
    public final int termArity;

    public UnresolvedStrategy(IStrategoTerm locationTerm, int strategyArity, int termArity,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.strategyArity = strategyArity;
        this.termArity = termArity;
    }

    @Override
    public String getMessage() {
        return "Unresolved strategy with arity " + strategyArity + "/" + termArity + ".";
    }
}
