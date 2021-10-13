package benchmark.stratego2.execution;

import benchmark.stratego2.problem.BenchtreeProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 10, timeUnit = TimeUnit.MINUTES)
public class Benchtree extends ExecutionBenchmark implements BenchtreeProblem {

    @Param({"2", "4", "6", "7", /*"8", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22"*/})
    int problemSize;

}
