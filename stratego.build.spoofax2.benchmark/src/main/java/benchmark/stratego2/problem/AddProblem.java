package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface AddProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "add%d.str2";
    }
}
