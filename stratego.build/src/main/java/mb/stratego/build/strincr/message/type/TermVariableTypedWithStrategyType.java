package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class TermVariableTypedWithStrategyType extends TypeMessage<IStrategoTerm> {
    public TermVariableTypedWithStrategyType(IStrategoTerm callDynTerm, MessageSeverity severity,
        long lastModified) {
        super(callDynTerm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "This is a term variable, but it has a strategy type.";
    }
}
