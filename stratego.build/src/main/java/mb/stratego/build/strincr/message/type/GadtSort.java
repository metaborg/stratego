package mb.stratego.build.strincr.message.type;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class GadtSort extends TypeMessage<IStrategoTerm> {
    public GadtSort(IStrategoTerm subterm, MessageSeverity severity, long lastModified) {
        super(subterm, severity, lastModified);
    }

    @Override public String getMessage() {
        return "Must use all unique sort variables as sort arguments, GADTs are not supported. ";
    }
}
