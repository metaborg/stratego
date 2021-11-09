package benchmark.stratego2.execution;

import benchmark.stratego2.problem.MergesortProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 10, timeUnit = TimeUnit.MINUTES)
public class Mergesort extends StrategoExecutionBenchmark implements MergesortProblem {

    @Param({"10", "20", "30", "40", /*"50", "100", "200", "300", "500", "720", "1000"*/})
    int problemSize;

}
