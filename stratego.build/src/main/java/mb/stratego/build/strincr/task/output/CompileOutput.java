package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.message.Message;

public interface CompileOutput extends Serializable {
    boolean equals(Object o);
    int hashCode();
    String toString();

    class Success implements CompileOutput {
        public final Set<ResourcePath> resultFiles;

        public Success(Set<ResourcePath> resultFiles) {
            this.resultFiles = resultFiles;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Success output = (Success) o;

            return resultFiles.equals(output.resultFiles);
        }

        @Override public int hashCode() {
            return resultFiles.hashCode();
        }

        @Override public String toString() {
            return "Compile.Success(" + resultFiles.size() + ")";
        }
    }

    class Failure implements CompileOutput {
        public final List<Message<?>> messages;

        public Failure(List<Message<?>> messages) {
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
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
