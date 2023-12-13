package benchmark.til.execution;

import java.util.Collection;
import java.util.Collections;

import benchmark.til.problem.Add200Problem;

public class Add200 extends TILExecutionBenchmark implements Add200Problem {
    @Override protected Collection<String> input() {
        return Collections.singletonList("\"0\"");
    }
}
