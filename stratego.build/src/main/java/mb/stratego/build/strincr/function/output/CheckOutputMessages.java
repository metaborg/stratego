package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.List;

import mb.stratego.build.strincr.message.Message2;

public class CheckOutputMessages implements Serializable {
    public final List<Message2<?>> messages;
    public final boolean containsErrors;

    public CheckOutputMessages(List<Message2<?>> messages, boolean containsErrors) {
        this.messages = messages;
        this.containsErrors = containsErrors;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckOutputMessages messages1 = (CheckOutputMessages) o;

        if(containsErrors != messages1.containsErrors)
            return false;
        return messages.equals(messages1.messages);
    }

    @Override public int hashCode() {
        int result = messages.hashCode();
        result = 31 * result + (containsErrors ? 1 : 0);
        return result;
    }
}
