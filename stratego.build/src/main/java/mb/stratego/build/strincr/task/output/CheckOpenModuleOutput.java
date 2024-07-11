package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.Message;

public class CheckOpenModuleOutput implements Serializable {
    public final @Nullable IStrategoTerm astWithCasts;
    public final ArrayList<Message> messages;
    protected final int hashCode;

    public CheckOpenModuleOutput(
        @Nullable IStrategoTerm astWithCasts,
        ArrayList<Message> messages) {
        this.astWithCasts = astWithCasts;
        this.messages = messages;
        this.hashCode = hashFunction();
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckOpenModuleOutput that = (CheckOpenModuleOutput) o;

        if(hashCode != that.hashCode)
            return false;
        if(!Objects.equals(astWithCasts, that.astWithCasts))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = astWithCasts != null ? astWithCasts.hashCode() : 0;
        result = 31 * result + messages.hashCode();
        return result;
    }

    @Override public String toString() {
        if(astWithCasts == null) {
            assert messages.isEmpty();
            return "CheckOpenModuleOutput@" + System.identityHashCode(this) + "{inputIsALibrary=true}";
        }
        //@formatter:off
        return "CheckOpenModuleOutput@" + System.identityHashCode(this) + '{'
            + "astWithCasts=" + astWithCasts.toString(4)
            + ", messages=" + messages.size()
            + '}';
        //@formatter:on
    }
}
