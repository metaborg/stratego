package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BubblesortProblem extends Problem {

    @Override
    default String problemFileName() {
        return "bubblesort.str2";
    }
}
