package mb.stratego.build.strincr.function;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.task.output.CompileDynamicRulesOutput;

public class DynamicCallsDefined implements SerializableFunction<CompileDynamicRulesOutput, Boolean> {
    public static final DynamicCallsDefined INSTANCE = new DynamicCallsDefined();

    private DynamicCallsDefined() {
    }

    @Override public Boolean apply(CompileDynamicRulesOutput compileDynamicRulesOutput) {
        return !compileDynamicRulesOutput.newGenerated.isEmpty() || !compileDynamicRulesOutput.undefineGenerated.isEmpty();
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
