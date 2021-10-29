package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.SieveProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Sieve extends StrategoCompilationBenchmark implements SieveProblem {

    @Param({"20", "40", "60", "80", "100", /*"1000", "2000", "100000"*/})
    int problemSize;

}
