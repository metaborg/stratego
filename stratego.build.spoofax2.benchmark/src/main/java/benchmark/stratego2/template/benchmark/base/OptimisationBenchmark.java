package benchmark.stratego2.template.benchmark.base;

import org.openjdk.jmh.annotations.Param;

public abstract class OptimisationBenchmark extends BaseBenchmark {
    @SuppressWarnings("unused")
    @Param({"2", "3", "4"})
    int optimisationLevel = -1;

//    @SuppressWarnings("unused")
//    @Param({"on", "off"})
//    String fusion = "";
}
