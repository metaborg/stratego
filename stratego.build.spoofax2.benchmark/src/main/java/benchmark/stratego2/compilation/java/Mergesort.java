package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.MergesortProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Mergesort extends JavaCompilationBenchmark implements MergesortProblem {

    @Param({"10", "20", "30", "40", /*"50", "100", "200", "300", "500", "720", "1000"*/})
    int problemSize;

}
