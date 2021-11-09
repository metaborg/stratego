package benchmark.stratego2.execution.failing;

import benchmark.stratego2.problem.AddProblem;
import benchmark.stratego2.execution.StrategoExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Add extends StrategoExecutionBenchmark implements AddProblem {

    @Param({"8", "16", "32"})
    int problemSize;

}
