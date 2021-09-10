package benchmark.stratego2.execution;

import benchmark.stratego2.problem.HanoiProblem;
import benchmark.stratego2.template.benchmark.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 2, timeUnit = TimeUnit.MINUTES)
public class Hanoi extends ExecutionBenchmark implements HanoiProblem {

    @Param({"4", "5", "6", "7", "8", "9", "10", "11"/*, "12", "16", "20"*/})
    int problemSize;

}
