package mb.stratego.build.strincr.function;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.task.output.CheckOutput;

public class ContainsErrors implements SerializableFunction<CheckOutput, Boolean> {
    public static final ContainsErrors INSTANCE = new ContainsErrors();

    private ContainsErrors() {
    }

    @Override public Boolean apply(CheckOutput output) {
        return output.containsErrors;
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
