package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface CallsProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "calls.str2";
    }
}
