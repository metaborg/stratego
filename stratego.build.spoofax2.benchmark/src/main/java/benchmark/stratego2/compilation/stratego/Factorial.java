package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.FactorialProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Factorial extends StrategoCompilationBenchmark implements FactorialProblem {

    @Param({"4", "5", "6", "7", /*"8", "9"*/})
    int problemSize;

}
