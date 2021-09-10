package benchmark.stratego2.execution;

import benchmark.stratego2.problem.FactorialProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 30, timeUnit = TimeUnit.MINUTES)
public class Factorial extends ExecutionBenchmark implements FactorialProblem {

    @Param({"5", "6", "7"/*, "8", "9"*/})
    int problemSize;

}
