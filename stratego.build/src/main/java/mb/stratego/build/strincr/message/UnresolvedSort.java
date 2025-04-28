package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedSort extends Message {
    private final String sortName;
    public final int arity;

    public UnresolvedSort(IStrategoTerm locationTerm, String sortName, int arity, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.sortName = sortName;
        this.arity = arity;
    }

    @Override
    public String getMessage() {
        return "Unresolved sort " + sortName + " with arity " + arity + ".";
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        UnresolvedSort that = (UnresolvedSort) o;

        return arity == that.arity;
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + arity;
        return result;
    }
}
