package benchmark.til.execution;

import java.util.Collection;
import java.util.Collections;

import benchmark.til.problem.Add500Problem;

public class Add500 extends TILExecutionBenchmark implements Add500Problem {
    @Override protected Collection<String> input() {
        return Collections.singletonList("\"0\"");
    }
}
