package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class StrategyVariableTypedWithTermType extends StrategoMessage {
    public StrategyVariableTypedWithTermType(String module, IStrategoTerm callDynTerm, MessageSeverity severity) {
        super(module, callDynTerm, severity);
    }

    @Override
    public String getMessageWithoutLocation() {
        return "This is a strategy variable, but it has a term type.";
    }
}
