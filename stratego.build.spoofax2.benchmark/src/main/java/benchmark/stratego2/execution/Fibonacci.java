package benchmark.stratego2.execution;

import benchmark.stratego2.problem.FibonacciProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Fibonacci extends ExecutionBenchmark implements FibonacciProblem {

    @Param({"18", "19", "20", "21"})
    int problemSize;

}
