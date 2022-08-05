package benchmark.til.execution;

import java.util.Collection;
import java.util.Collections;

import org.openjdk.jmh.annotations.Param;

import benchmark.til.problem.FactorialProblem;

public class Factorial extends TILExecutionBenchmark implements FactorialProblem {
    @Param({"4", "5", "6", "7", "8", "9"})
    int problemSize;

    @Override protected Collection<String> input() {
        return Collections.singletonList('"' + Integer.toString(problemSize) + '"');
    }
}
