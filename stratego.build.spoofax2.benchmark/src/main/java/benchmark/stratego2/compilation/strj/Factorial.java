package benchmark.stratego2.compilation.strj;

import benchmark.stratego2.problem.FactorialProblem;
import benchmark.stratego2.template.benchmark.compilation.StrjCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Factorial extends StrjCompilationBenchmark implements FactorialProblem {

    @Param({"4", "5", "6", "7", /*"8", "9"*/})
    int problemSize;

}
