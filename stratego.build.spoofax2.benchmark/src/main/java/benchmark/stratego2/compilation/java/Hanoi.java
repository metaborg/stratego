package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.HanoiProblem;
import org.openjdk.jmh.annotations.Param;

public class Hanoi extends JavaCompilationBenchmark implements HanoiProblem {

    @Param({"4", "5", "6", "7", "8", "9", "10", "11"/*, "12", "16", "20"*/})
    int problemSize;

}
