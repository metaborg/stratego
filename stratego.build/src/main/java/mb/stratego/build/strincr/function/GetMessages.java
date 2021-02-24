package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.function.Function;

import mb.stratego.build.strincr.function.output.CheckOutputMessages;
import mb.stratego.build.strincr.task.output.CheckOutput;

public class GetMessages implements Function<CheckOutput, CheckOutputMessages>, Serializable {
    public static final GetMessages INSTANCE = new GetMessages();

    private GetMessages() {
    }

    @Override public CheckOutputMessages apply(CheckOutput output) {
        return new CheckOutputMessages(output.messages, output.containsErrors);
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
