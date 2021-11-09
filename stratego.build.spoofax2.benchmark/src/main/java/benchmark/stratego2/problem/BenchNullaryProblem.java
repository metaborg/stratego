package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BenchNullaryProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchnullary%d.str2";
    }
}
