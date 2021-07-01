package benchmark.stratego2.failing;

import benchmark.stratego2.problem.BenchexprProblem;
import benchmark.stratego2.template.benchmark.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class BenchexprExecution extends ExecutionBenchmark implements BenchexprProblem {

    @Param({"10", "15", "20", "22"})
    int problemSize;

}
