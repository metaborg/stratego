package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface FibonacciProblem extends Problem {
    @Override
    default String problemFileName() {
        return "fibonacci.str2";
    }
}
