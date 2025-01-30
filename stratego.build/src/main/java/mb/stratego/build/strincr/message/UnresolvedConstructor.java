package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedConstructor extends Message {
    private final String consName;
    public final int arity;
    public final String sort;

    public UnresolvedConstructor(IStrategoTerm locationTerm, String consName, int arity, IStrategoTerm sort,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.consName = consName;
        this.arity = arity;
        this.sort = sort.toString();
    }

    @Override
    public String getMessage() {
        return "Undefined constructor " + consName + " with arity " + arity + " and type " + sort + ".";
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        UnresolvedConstructor that = (UnresolvedConstructor) o;

        if(arity != that.arity)
            return false;
        return sort.equals(that.sort);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + arity;
        result = 31 * result + sort.hashCode();
        return result;
    }
}
