package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BenchtreeProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchtree%d.str2";
    }
}
