package mb.stratego.build.strincr.function;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.function.output.CheckOutputMessages;
import mb.stratego.build.strincr.task.output.CheckOutput;

public class GetCheckMessages implements SerializableFunction<CheckOutput, CheckOutputMessages> {
    public static final GetCheckMessages INSTANCE = new GetCheckMessages();

    private GetCheckMessages() {
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
