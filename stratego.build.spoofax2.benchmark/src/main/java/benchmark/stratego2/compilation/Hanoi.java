package benchmark.stratego2.compilation;

import benchmark.stratego2.problem.HanoiProblem;
import benchmark.stratego2.template.benchmark.CompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Hanoi extends CompilationBenchmark implements HanoiProblem {

    @Param({"4", "8", "12", "16", "20"})
    int problemSize;

}
