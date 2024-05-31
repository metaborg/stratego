package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface MergesortProblem extends Problem {

    @Override
    default String problemFileName() {
        return "mergesort.str2";
    }

}
