package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface HanoiProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "hanoi%d.str2";
    }
}
