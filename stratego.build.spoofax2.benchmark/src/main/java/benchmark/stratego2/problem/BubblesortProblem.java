package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface BubblesortProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "bubblesort%d.str2";
    }
}
