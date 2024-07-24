package benchmark.stratego2.execution;

import benchmark.stratego2.problem.FibonacciProblem;
import org.openjdk.jmh.annotations.Param;

public class Fibonacci extends StrategoExecutionBenchmark implements FibonacciProblem {

    @Param({"18", "19", "20", "21"})
    int problemSize;

}
