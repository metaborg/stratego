package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class StrategyVariableTypedWithTermType extends TypeMessage<IStrategoTerm> {
    public StrategyVariableTypedWithTermType(IStrategoTerm callDynTerm, MessageSeverity severity, long lastModified) {
        super(callDynTerm, severity, lastModified);
    }

    @Override
    public String getMessage() {
        return "This is a strategy variable, but it has a term type.";
    }
}
