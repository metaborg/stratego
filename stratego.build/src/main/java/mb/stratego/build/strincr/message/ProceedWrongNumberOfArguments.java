package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class ProceedWrongNumberOfArguments extends Message<IStrategoTerm> {
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
}
