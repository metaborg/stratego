package benchmark.stratego2.compilation;

import benchmark.stratego2.problem.BubblesortProblem;
import benchmark.stratego2.template.benchmark.CompilationBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 30, timeUnit = TimeUnit.MINUTES)
public class Bubblesort extends CompilationBenchmark implements BubblesortProblem {

    @Param({"10", "20", "50", "100", "200"/*, "300", "500", "720", "1000"*/})
    int problemSize;

}
