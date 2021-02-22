package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedConstructor extends Message<IStrategoTerm> {
    public final int arity;
    public final IStrategoTerm sort;

    public UnresolvedConstructor(IStrategoTerm locationTerm, int arity, IStrategoTerm sort,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.arity = arity;
        this.sort = sort;
    }

    @Override
    public String getMessage() {
        return "Undefined constructor with arity " + arity + " and type " + sort + ".";
    }
}
