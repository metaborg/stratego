package benchmark.stratego2.execution;

import benchmark.stratego2.problem.QuicksortProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Quicksort extends ExecutionBenchmark implements QuicksortProblem {

    @Param({"10", "100", "1000"})
    int problemSize;

}
