package benchmark.stratego2.template.benchmark.base;

import org.openjdk.jmh.annotations.Param;

public abstract class OptimisationBenchmark extends BaseBenchmark {
    @SuppressWarnings("unused")
    @Param({"2", "3", "4"})
    public int optimisationLevel = -1;

    @Param({"", "elseif", "nested-switch", "hash-switch"})
    public String switchImplementation = "";

//    @Param({"", /*"name-arity",*/ "arity-name"})
//    public String switchImplementationOrder = "";

//    @SuppressWarnings("unused")
//    @Param({"on", "off"})
//    String fusion = "";
}
