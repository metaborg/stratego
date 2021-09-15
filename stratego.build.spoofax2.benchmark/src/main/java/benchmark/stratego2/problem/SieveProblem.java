package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface SieveProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "sieve%d.str2";
    }
}
