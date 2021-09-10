package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.HanoiProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Hanoi extends StrategoCompilationBenchmark implements HanoiProblem {

    @Param({"4", "5", "6", "7", "8", "9", "10", "11"/*, "12", "16", "20"*/})
    int problemSize;

}
