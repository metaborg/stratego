package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface FibonacciProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "fibonacci%d.str2";
    }
}
