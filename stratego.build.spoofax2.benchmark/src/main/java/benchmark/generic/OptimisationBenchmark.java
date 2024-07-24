package benchmark.generic;

import org.openjdk.jmh.annotations.Param;

public abstract class OptimisationBenchmark<P extends Program<? extends api.Compiler<?>>> extends BaseBenchmark<P> {
    @SuppressWarnings({"unused", "CanBeFinal"})
    @Param({"2", "3", "4"})
    public int optimisationLevel = -1;
}
