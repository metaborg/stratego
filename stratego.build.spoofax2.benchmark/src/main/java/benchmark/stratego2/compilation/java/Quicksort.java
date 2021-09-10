package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.QuicksortProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Quicksort extends JavaCompilationBenchmark implements QuicksortProblem {

    @Param({"10", "100", "1000"})
    int problemSize;

}
