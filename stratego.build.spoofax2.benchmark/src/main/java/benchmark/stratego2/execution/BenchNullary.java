package benchmark.stratego2.execution;

import benchmark.stratego2.problem.BenchNullaryProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
public class BenchNullary extends StrategoExecutionBenchmark implements BenchNullaryProblem {

    @Param({"100", "200", "300", "400", "500", /*"600", "700", "800", "900", "1000"*/})
    int problemSize;

}
