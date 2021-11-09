package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.FactorialProblem;
import org.openjdk.jmh.annotations.Param;

public class Factorial extends JavaCompilationBenchmark implements FactorialProblem {

    @Param({"4", "5", "6", "7", /*"8", "9"*/})
    int problemSize;

}
