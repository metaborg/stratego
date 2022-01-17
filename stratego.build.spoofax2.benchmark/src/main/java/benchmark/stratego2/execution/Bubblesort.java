package benchmark.stratego2.execution;

import benchmark.stratego2.problem.BubblesortProblem;
import org.openjdk.jmh.annotations.Param;

public class Bubblesort extends StrategoExecutionBenchmark implements BubblesortProblem {

    @Param({"10", "20", "50", "100", "200", /*"300", "500", "720", "1000"*/})
    int problemSize;

}
