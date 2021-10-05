package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.BenchexprProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Benchexpr extends StrategoCompilationBenchmark implements BenchexprProblem {

    @Param({"10", "11", "12", "13", "14", "15", "16", "17", /*"18", "19", "20", "22" */})
    int problemSize;

}
