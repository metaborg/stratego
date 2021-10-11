package benchmark.stratego2.execution;

import benchmark.stratego2.problem.MergesortProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public class Mergesort extends ExecutionBenchmark implements MergesortProblem {

    @Param({"10", "15", /*"100", "1000"*/})
    int problemSize;

}
