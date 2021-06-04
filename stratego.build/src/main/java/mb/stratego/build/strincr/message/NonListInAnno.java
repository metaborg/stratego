package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class NonListInAnno extends Message {
    public final String type;

    public NonListInAnno(IStrategoTerm locationTerm, IStrategoTerm type, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.type = type.toString();
    }

    @Override
    public String getMessage() {
        return "Expected List, but got " + type;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        NonListInAnno that = (NonListInAnno) o;

        return type.equals(that.type);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
