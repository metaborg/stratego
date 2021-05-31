package mb.stratego.build.termvisitors;

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

    @Override public boolean equals(Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
