package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.BenchexprProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Benchexpr extends JavaCompilationBenchmark implements BenchexprProblem {

    @Param({"10", "15", "20", "22"})
    int problemSize;

}
