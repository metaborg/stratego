package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.MergesortProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Mergesort extends StrategoCompilationBenchmark implements MergesortProblem {

    @Param({"10", "100", "1000"})
    int problemSize;

}
