package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface BenchtreeProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchtree%d.str2";
    }
}
