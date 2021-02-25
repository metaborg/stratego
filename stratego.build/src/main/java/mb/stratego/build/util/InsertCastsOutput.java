package mb.stratego.build.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

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
        if(getClass() != o.getClass())
            return false;
        InsertCastsOutput output = (InsertCastsOutput) o;
        return astWithCasts.equals(output.astWithCasts) && messages.equals(output.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(astWithCasts, messages);
    }
}
