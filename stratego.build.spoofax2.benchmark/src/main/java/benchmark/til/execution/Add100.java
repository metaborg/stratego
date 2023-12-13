package benchmark.til.execution;

import java.util.Collection;
import java.util.Collections;

import benchmark.til.problem.Add100Problem;

public class Add100 extends TILExecutionBenchmark implements Add100Problem {
    @Override protected Collection<String> input() {
        return Collections.singletonList("\"0\"");
    }
}
