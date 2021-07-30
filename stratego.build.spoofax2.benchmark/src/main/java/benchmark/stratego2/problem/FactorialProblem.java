package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface FactorialProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "factorial%d.str2";
    }
}
