package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface QuicksortProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "quicksort%d.str2";
    }
}
