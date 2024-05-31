package benchmark.til.problem;

import benchmark.generic.Problem;

public interface FactorialProblem extends Problem {
    @Override
    default String problemFileName() {
        return "factorial.til";
    }
}
