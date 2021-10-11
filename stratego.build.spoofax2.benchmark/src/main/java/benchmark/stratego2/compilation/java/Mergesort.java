package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.MergesortProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Mergesort extends JavaCompilationBenchmark implements MergesortProblem {

    @Param({"10", "15", /*"100", "1000"*/})
    int problemSize;

}
