package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.Message;

public class CheckOpenModuleOutput implements Serializable {
    public final @Nullable IStrategoTerm astWithCasts;
    public final ArrayList<Message> messages;

    public CheckOpenModuleOutput(
        @Nullable IStrategoTerm astWithCasts,
        ArrayList<Message> messages) {
        this.astWithCasts = astWithCasts;
        this.messages = messages;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CheckOpenModuleOutput that = (CheckOpenModuleOutput) o;

        if(!Objects.equals(astWithCasts, that.astWithCasts))
            return false;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
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
