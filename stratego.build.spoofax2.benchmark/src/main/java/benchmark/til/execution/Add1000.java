package benchmark.til.execution;

import java.util.Collection;
import java.util.Collections;

import benchmark.til.problem.Add1000Problem;

public class Add1000 extends TILExecutionBenchmark implements Add1000Problem {
    @Override protected Collection<String> input() {
        return Collections.singletonList("\"0\"");
    }
}
