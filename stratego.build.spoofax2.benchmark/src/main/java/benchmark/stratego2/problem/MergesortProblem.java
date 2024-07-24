package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface MergesortProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "mergesort%d.str2";
    }

}
