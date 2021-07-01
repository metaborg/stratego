package benchmark.stratego2.failing;

import benchmark.stratego2.problem.AddProblem;
import benchmark.stratego2.template.benchmark.CompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class AddCompilation extends CompilationBenchmark implements AddProblem {

    @Param({"8", "16", "32"})
    int problemSize;

}
