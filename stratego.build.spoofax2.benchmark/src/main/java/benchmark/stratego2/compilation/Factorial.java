package benchmark.stratego2.compilation;

import benchmark.stratego2.problem.FactorialProblem;
import benchmark.stratego2.template.benchmark.CompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Factorial extends CompilationBenchmark implements FactorialProblem {

    @Param({"5", "6", "7", "8", "9"})
    int problemSize;

}
