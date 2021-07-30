package benchmark.stratego2.compilation;

import benchmark.stratego2.problem.QuicksortProblem;
import benchmark.stratego2.template.benchmark.CompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Quicksort extends CompilationBenchmark implements QuicksortProblem {

    @Param({"10", "100", "1000"})
    int problemSize;

}
