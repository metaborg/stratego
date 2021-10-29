package benchmark.stratego2.execution;

import benchmark.stratego2.problem.BenchsymProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 10, timeUnit = TimeUnit.MINUTES)
public class Benchsym extends ExecutionBenchmark implements BenchsymProblem {

    @Param({"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", /*"22" */})
    int problemSize;

}
