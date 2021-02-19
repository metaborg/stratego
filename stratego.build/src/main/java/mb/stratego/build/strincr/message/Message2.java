package mb.stratego.build.strincr.message;

import java.io.Serializable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.util.WithLastModified;

public abstract class Message2<T extends IStrategoTerm> implements WithLastModified, Serializable {
    public final T locationTerm;
    public final ImploderAttachment location;
    public final MessageSeverity severity;
    public final long lastModified;

    public Message2(T name, MessageSeverity severity, long lastModified) {
        this.locationTerm = name;
        this.location = ImploderAttachment.get(OriginAttachment.tryGetOrigin(name));
        assert this.location != null : "The given term " + name + " did not contain a location";
        this.severity = severity;
        this.lastModified = lastModified;
    }

    public static Message2<?> from(Message<?> message) {
        // TODO get rid of this method and use Message2 in InsertCastss
        return new Message2<IStrategoTerm>(message.locationTerm, message.severity, 0L) {
            @Override public String getMessage() {
                return message.getMessage();
            }
        };
    }

    public String toString() {
        return "In '" + locationString() + "':\n" + getMessage();
    }

    public String locationString() {
        final IToken leftToken = location.getLeftToken();
        final IToken rightToken = location.getRightToken();
        final String filename = leftToken.getFilename();
        final int leftLine = leftToken.getLine();
        final int leftColumn = leftToken.getColumn();
        final int rightLine = rightToken.getEndLine();
        final int rightColumn = rightToken.getEndColumn();
        if(leftLine == rightLine) {
            if(leftColumn == rightColumn) {
                return filename + ":" + leftLine + ":" + leftColumn;
            }
            return filename + ":" + leftLine + ":" + leftColumn + "-" + rightColumn;
        } else {
            return filename + ":" + leftLine + "-" + rightLine + ":" + leftColumn + "-" + rightColumn;
        }
    }

    public String moduleFilePath() {
        final IToken leftToken = location.getLeftToken();
        return leftToken.getFilename();
    }

    public abstract String getMessage();

    public long lastModified() {
        return lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Message2<?> message = (Message2<?>) o;
        if(!locationTerm.equals(message.locationTerm)) return false;
        if(severity != message.severity) return false;
        return getMessage().equals(message.getMessage());
    }

    @Override public int hashCode() {
        int result = locationTerm.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result +  getMessage().hashCode();
        return result;
    }
}