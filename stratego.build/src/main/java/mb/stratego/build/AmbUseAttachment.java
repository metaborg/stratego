package mb.stratego.build;

import org.spoofax.terms.attachments.AbstractTermAttachment;
import org.spoofax.terms.attachments.TermAttachmentType;
import org.spoofax.terms.attachments.VolatileTermAttachmentType;

public class AmbUseAttachment extends AbstractTermAttachment {
    static final AmbUseAttachment INSTANCE = new AmbUseAttachment();
    static final TermAttachmentType<AmbUseAttachment> TYPE = new VolatileTermAttachmentType<>(AmbUseAttachment.class);

    @Override public TermAttachmentType<AmbUseAttachment> getAttachmentType() {
        return TYPE;
    }

    private AmbUseAttachment() {
    }
}
