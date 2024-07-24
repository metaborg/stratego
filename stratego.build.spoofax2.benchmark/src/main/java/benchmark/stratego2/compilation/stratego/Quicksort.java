package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.QuicksortProblem;
import org.openjdk.jmh.annotations.Param;

public class Quicksort extends StrategoCompilationBenchmark implements QuicksortProblem {

    @Param({"10", "12", "14", "16", "18", "20", /*"100", "1000"*/})
    int problemSize;

}
