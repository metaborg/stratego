package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface QuicksortProblem extends Problem {

    @Override
    default String problemFileName() {
        return "quicksort.str2";
    }
}
