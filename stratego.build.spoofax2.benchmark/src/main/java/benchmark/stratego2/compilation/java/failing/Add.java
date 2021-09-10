package benchmark.stratego2.compilation.java.failing;

import benchmark.stratego2.problem.AddProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Add extends JavaCompilationBenchmark implements AddProblem {

    @Param({"8", "16", "32"})
    int problemSize;

}
