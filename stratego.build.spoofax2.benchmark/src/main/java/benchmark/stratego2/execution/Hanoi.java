package benchmark.stratego2.execution;

import benchmark.stratego2.problem.HanoiProblem;
import benchmark.stratego2.template.benchmark.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 30, timeUnit = TimeUnit.MINUTES)
public class Hanoi extends ExecutionBenchmark implements HanoiProblem {

    @Param({"4", "8", "12", "16", "20"})
    int problemSize;

}
