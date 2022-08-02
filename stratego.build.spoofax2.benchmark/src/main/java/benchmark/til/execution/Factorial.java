package benchmark.til.execution;

import java.util.Collection;
import java.util.Collections;

import benchmark.til.problem.FactorialProblem;

public class Factorial extends TILExecutionBenchmark implements FactorialProblem {
    @Override protected Collection<String> input() {
        return Collections.emptyList();
    }
}
