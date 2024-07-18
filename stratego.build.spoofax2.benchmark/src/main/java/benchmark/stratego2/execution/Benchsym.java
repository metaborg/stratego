package benchmark.stratego2.execution;

import benchmark.stratego2.problem.BenchsymProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 10, timeUnit = TimeUnit.MINUTES)
public class Benchsym extends StrategoExecutionBenchmark implements BenchsymProblem {

    @Param({"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", /*"22" */})
    int problemSize;

}
