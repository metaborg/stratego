package benchmark.stratego2.execution;

import benchmark.stratego2.problem.HanoiProblem;
import benchmark.stratego2.template.benchmark.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class HanoiExecution extends ExecutionBenchmark implements HanoiProblem {

    @Param({"4", "8", "12", "16", "20"})
    int problemSize;

    public HanoiExecution() {
        super();
    }

    public HanoiExecution(int problemSize) {
        this.problemSize = problemSize;
    }
}
