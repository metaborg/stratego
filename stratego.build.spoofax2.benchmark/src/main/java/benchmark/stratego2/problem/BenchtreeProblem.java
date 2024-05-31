package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BenchtreeProblem extends Problem {

    @Override
    default String problemFileName() {
        return "benchtree.str2";
    }
}
