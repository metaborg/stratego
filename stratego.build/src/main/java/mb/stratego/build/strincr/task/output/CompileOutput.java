package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.annotation.Nullable;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.message.Message;

public interface CompileOutput extends Serializable {
    boolean equals(Object o);
    int hashCode();
    String toString();

    class Success implements CompileOutput {
        public final LinkedHashSet<ResourcePath> resultFiles;
        public final ArrayList<Message> messages;

        public Success(LinkedHashSet<ResourcePath> resultFiles, ArrayList<Message> messages) {
            this.resultFiles = resultFiles;
            this.messages = messages;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Success success = (Success) o;

            if(!resultFiles.equals(success.resultFiles))
                return false;
            return messages.equals(success.messages);
        }

        @Override public int hashCode() {
            int result = resultFiles.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Compile.Success(" + resultFiles.size() + ")";
        }
    }

    class Failure implements CompileOutput {
        public final ArrayList<Message> messages;

        public Failure(ArrayList<Message> messages) {
            this.messages = messages;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Failure output = (Failure) o;

            return messages.equals(output.messages);
        }

        @Override public int hashCode() {
            return messages.hashCode();
        }

        @Override public String toString() {
            return "Compile.Failure(" + messages.size() + ")";
        }
    }
}
