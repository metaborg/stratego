package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.QuicksortProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Quicksort extends StrategoCompilationBenchmark implements QuicksortProblem {

    @Param({"10", "100", "1000"})
    int problemSize;

}
