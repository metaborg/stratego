package mb.stratego.build.strincr.message;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

public class ExternalStrategyNotFound extends Message {
    public ExternalStrategyNotFound(IStrategoString name, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Cannot find external strategy or rule '" + locationTermString + "'";
    }

    public static ExternalStrategyNotFound followOrigin(IStrategoString name, long lastModified) {
        IStrategoString definitionName = name;
        @Nullable IStrategoTerm origin = OriginAttachment.getOrigin(definitionName);
        while(origin != null && TermUtils.isString(origin)) {
            definitionName = (IStrategoString) origin;
            origin = OriginAttachment.getOrigin(definitionName);
        }
        return new ExternalStrategyNotFound(definitionName, lastModified);
    }
}
