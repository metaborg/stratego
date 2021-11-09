package benchmark.stratego2.execution;

import benchmark.stratego2.problem.SieveProblem;
import org.openjdk.jmh.annotations.Param;

public class Sieve extends StrategoExecutionBenchmark implements SieveProblem {

    @Param({"20", "40", "60", "80", "100", /*"1000", "2000", "100000"*/})
    int problemSize;

}
