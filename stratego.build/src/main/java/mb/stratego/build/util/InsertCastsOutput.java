package mb.stratego.build.util;

import java.io.Serializable;
import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.Message;

public final class InsertCastsOutput implements Serializable {
    public final IStrategoTerm astWithCasts;
    public final ArrayList<Message> messages;

    public InsertCastsOutput(IStrategoTerm astWithCasts, ArrayList<Message> messages) {
        this.astWithCasts = astWithCasts;
        this.messages = messages;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        InsertCastsOutput that = (InsertCastsOutput) o;

        if(!astWithCasts.equals(that.astWithCasts))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        int result = astWithCasts.hashCode();
        result = 31 * result + messages.hashCode();
        return result;
    }
}
