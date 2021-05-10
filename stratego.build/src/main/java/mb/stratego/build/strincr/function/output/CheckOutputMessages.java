package mb.stratego.build.strincr.function.output;

import java.io.Serializable;
import java.util.ArrayList;

import mb.stratego.build.strincr.message.Message;

public class CheckOutputMessages implements Serializable {
    public final ArrayList<Message> messages;
    public final boolean containsErrors;

    public CheckOutputMessages(ArrayList<Message> messages, boolean containsErrors) {
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

    @Override public String toString() {
        return "CheckOutputMessages(" + messages.size() + ", " + containsErrors + ')';
    }
}
