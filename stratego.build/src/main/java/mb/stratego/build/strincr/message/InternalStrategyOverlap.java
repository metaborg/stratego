package mb.stratego.build.strincr.message;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

public class InternalStrategyOverlap extends Message {
    public InternalStrategyOverlap(IStrategoString name, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Strategy '" + locationTermString + "' overlaps with a strategy defined to be internal";
    }

    public static InternalStrategyOverlap followOrigin(IStrategoString name, long lastModified) {
        IStrategoString definitionName = name;
        @Nullable IStrategoTerm origin = OriginAttachment.getOrigin(definitionName);
        while(origin != null && TermUtils.isString(origin)) {
            definitionName = (IStrategoString) origin;
            origin = OriginAttachment.getOrigin(definitionName);
        }
        return new InternalStrategyOverlap(definitionName, lastModified);
    }
}
