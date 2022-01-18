package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface HanoiProblem extends Problem {

    @Override
    default String problemFileName() {
        return "hanoi.str2";
    }
}
