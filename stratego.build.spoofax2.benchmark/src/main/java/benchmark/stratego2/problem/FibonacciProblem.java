package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface FibonacciProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "fibonacci%d.str2";
    }
}
