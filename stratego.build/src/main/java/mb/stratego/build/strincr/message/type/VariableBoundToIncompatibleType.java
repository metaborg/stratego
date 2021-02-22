package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class VariableBoundToIncompatibleType extends TypeMessage<IStrategoTerm> {
    public final IStrategoTerm boundType;
    public final IStrategoTerm conflictingType;

    public VariableBoundToIncompatibleType(IStrategoTerm locationTerm, IStrategoTerm boundType,
        IStrategoTerm conflictingType, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.boundType = boundType;
        this.conflictingType = conflictingType;
    }

    @Override
    public String getMessage() {
        return "This variable has type " + boundType + ", which cannot be converted to " + conflictingType + " automatically.";
    }
}
