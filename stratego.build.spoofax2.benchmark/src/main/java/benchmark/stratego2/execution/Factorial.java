package benchmark.stratego2.execution;

import benchmark.stratego2.problem.FactorialProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Factorial extends ExecutionBenchmark implements FactorialProblem {

    @Param({"5", "6", "7", "8", "9"})
    int problemSize;

}
