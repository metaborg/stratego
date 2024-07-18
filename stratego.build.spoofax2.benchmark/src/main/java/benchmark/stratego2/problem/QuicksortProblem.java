package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface QuicksortProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "quicksort%d.str2";
    }
}
