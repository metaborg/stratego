package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.BenchexprProblem;
import org.openjdk.jmh.annotations.Param;

public class Benchexpr extends JavaCompilationBenchmark implements BenchexprProblem {

    @Param({"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", /*"22" */})
    int problemSize;

}
