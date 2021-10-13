package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.BenchtreeProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Benchtree extends JavaCompilationBenchmark implements BenchtreeProblem {

    @Param({"2", "4", "6", "7", /*"8", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22"*/})
    int problemSize;

}
