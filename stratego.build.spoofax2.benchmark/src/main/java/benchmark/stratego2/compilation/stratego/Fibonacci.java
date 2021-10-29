package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.FibonacciProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Fibonacci extends StrategoCompilationBenchmark implements FibonacciProblem {

    @Param({"18", "19", "20", "21"})
    int problemSize;

}
