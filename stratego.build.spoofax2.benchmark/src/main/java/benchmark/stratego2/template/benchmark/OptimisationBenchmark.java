package benchmark.stratego2.template.benchmark;

import org.openjdk.jmh.annotations.Param;

public abstract class OptimisationBenchmark extends BaseBenchmark {
    @SuppressWarnings("unused")
    @Param({/*"2",*/ "3", "4"})
    int optimisationLevel = -1;
}
