package benchmark.stratego2.compilation.stratego.failing;

import benchmark.stratego2.problem.AddProblem;
import benchmark.stratego2.template.benchmark.compilation.StrategoCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Add extends StrategoCompilationBenchmark implements AddProblem {

    @Param({"8", "16", "32"})
    int problemSize;

}
