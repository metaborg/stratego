package mb.stratego.build.strincr.message;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

public class ExternalStrategySourceNotFound extends Message {
    public ExternalStrategySourceNotFound(IStrategoString name, long lastModified) {
        super(name, MessageSeverity.ERROR, lastModified);
    }

    @Override public String getMessage() {
        return "Cannot find source of external strategy or rule '" + locationTermString + "'";
    }

    public static ExternalStrategySourceNotFound followOrigin(IStrategoString name, long lastModified) {
        IStrategoString definitionName = name;
        @Nullable IStrategoTerm origin = OriginAttachment.getOrigin(definitionName);
        while(origin != null && TermUtils.isString(origin)) {
            definitionName = (IStrategoString) origin;
            origin = OriginAttachment.getOrigin(definitionName);
        }
        return new ExternalStrategySourceNotFound(definitionName, lastModified);
    }
}
