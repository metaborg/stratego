package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BubblesortProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "bubblesort%d.str2";
    }
}
