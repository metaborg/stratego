package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class ProceedWrongNumberOfArguments extends StrategoMessage {
    private final int sarg;
    private final int targ;

    public ProceedWrongNumberOfArguments(String module, IStrategoTerm locationTerm, int sarg,
        int targ, MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.sarg = sarg;
        this.targ = targ;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "Proceed call expected " + sarg + " strategy arguments, and " + targ + " term arguments. ";
    }
}
