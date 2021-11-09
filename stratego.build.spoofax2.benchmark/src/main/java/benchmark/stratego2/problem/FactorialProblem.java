package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface FactorialProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "factorial%d.str2";
    }
}
