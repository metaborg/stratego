package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class ProceedWrongNumberOfArguments extends Message {
    private final int sarg;
    private final int targ;

    public ProceedWrongNumberOfArguments(IStrategoTerm locationTerm, int sarg,
        int targ, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.sarg = sarg;
        this.targ = targ;
    }

    @Override
    public String getMessage() {
        return "Proceed call expected " + sarg + " strategy arguments, and " + targ + " term arguments. ";
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        ProceedWrongNumberOfArguments that = (ProceedWrongNumberOfArguments) o;

        if(sarg != that.sarg)
            return false;
        return targ == that.targ;
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + sarg;
        result = 31 * result + targ;
        return result;
    }
}
