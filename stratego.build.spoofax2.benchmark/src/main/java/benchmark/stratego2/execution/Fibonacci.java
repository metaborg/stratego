package benchmark.stratego2.execution;

import benchmark.stratego2.problem.FibonacciProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 30, timeUnit = TimeUnit.SECONDS)
public class Fibonacci extends StrategoExecutionBenchmark implements FibonacciProblem {

    @Param({"18", "19", "20", "21"})
    int problemSize;

}
