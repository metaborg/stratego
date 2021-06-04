package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class RawTermMessage extends Message {
    private final String messageTerm;

    public RawTermMessage(IStrategoTerm locationTerm, IStrategoTerm messageTerm,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.messageTerm = messageTerm.toString();
    }

    @Override
    public String getMessage() {
        return this.messageTerm;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        RawTermMessage that = (RawTermMessage) o;

        return messageTerm.equals(that.messageTerm);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + messageTerm.hashCode();
        return result;
    }
}
