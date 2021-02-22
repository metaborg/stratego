package mb.stratego.build.strincr.message;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

public class ExternalStrategyOverlap extends Message<IStrategoString> {
    public ExternalStrategyOverlap(IStrategoString name, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Strategy '" + locationTerm.stringValue() + "' overlaps with an externally defined strategy";
    }

    public static ExternalStrategyOverlap followOrigin(IStrategoString name, long lastModified) {
        IStrategoString definitionName = name;
        @Nullable IStrategoTerm origin = OriginAttachment.getOrigin(definitionName);
        while(origin != null && TermUtils.isString(origin)) {
            definitionName = (IStrategoString) origin;
            origin = OriginAttachment.getOrigin(definitionName);
        }
        return new ExternalStrategyOverlap(definitionName, lastModified);
    }
}
