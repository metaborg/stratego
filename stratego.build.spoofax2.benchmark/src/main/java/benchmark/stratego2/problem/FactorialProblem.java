package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface FactorialProblem extends Problem {

    @Override
    default String problemFileName() {
        return "factorial.str2";
    }
}
