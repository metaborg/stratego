package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.SieveProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Sieve extends JavaCompilationBenchmark implements SieveProblem {

    @Param({"20", "100", "1000", "2000", "100000"})
    int problemSize;

}
