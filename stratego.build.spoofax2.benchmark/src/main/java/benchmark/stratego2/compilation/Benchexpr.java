package benchmark.stratego2.compilation;

import benchmark.stratego2.problem.BenchexprProblem;
import benchmark.stratego2.template.benchmark.CompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Benchexpr extends CompilationBenchmark implements BenchexprProblem {

    @Param({"10", "15", "20", "22"})
    int problemSize;

}
