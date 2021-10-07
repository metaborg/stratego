package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.BenchsymProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Benchsym extends StrategoCompilationBenchmark implements BenchsymProblem {

    @Param({"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22"})
    int problemSize;

}
