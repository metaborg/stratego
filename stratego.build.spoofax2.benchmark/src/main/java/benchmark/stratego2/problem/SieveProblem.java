package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface SieveProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "sieve%d.str2";
    }
}
