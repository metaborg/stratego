package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class CallStrategyArgumentTakesParameters extends StrategoMessage {
    public final IStrategoTerm sfuntype;

    public CallStrategyArgumentTakesParameters(String module, IStrategoTerm locationTerm, IStrategoTerm sfuntype,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.sfuntype = sfuntype;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "This call takes parameters, it has type: " + sfuntype;
    }
}
