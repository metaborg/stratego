package benchmark.stratego2.execution;

import benchmark.stratego2.problem.QuicksortProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public class Quicksort extends StrategoExecutionBenchmark implements QuicksortProblem {

    @Param({"10", "12", "14", "16", "18", "20", /*"100", "1000"*/})
    int problemSize;

}
