package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class VariableBoundToIncompatibleType extends StrategoMessage {
    public final IStrategoTerm boundType;
    public final IStrategoTerm conflictingType;

    public VariableBoundToIncompatibleType(String module, IStrategoTerm locationTerm, IStrategoTerm boundType,
        IStrategoTerm conflictingType, MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.boundType = boundType;
        this.conflictingType = conflictingType;
    }

    @Override
    public String getMessageWithoutLocation() {
        return "This variable has type " + boundType + ", which cannot be converted to " + conflictingType + " automatically.";
    }
}
