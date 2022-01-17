package benchmark.stratego2.execution;

import benchmark.stratego2.problem.FactorialProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
public class Factorial extends StrategoExecutionBenchmark implements FactorialProblem {

    @Param({"4", "5", "6", "7", /*"8", "9"*/})
    int problemSize;

}
