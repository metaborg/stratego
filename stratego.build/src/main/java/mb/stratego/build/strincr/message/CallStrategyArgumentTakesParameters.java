package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class CallStrategyArgumentTakesParameters extends Message {
    public final String sfuntype;

    public CallStrategyArgumentTakesParameters(IStrategoTerm locationTerm, IStrategoTerm sfuntype,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.sfuntype = sfuntype.toString();
    }

    @Override
    public String getMessage() {
        return "This call takes parameters, it has type: " + sfuntype;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        CallStrategyArgumentTakesParameters that = (CallStrategyArgumentTakesParameters) o;

        return sfuntype.equals(that.sfuntype);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + sfuntype.hashCode();
        return result;
    }
}
