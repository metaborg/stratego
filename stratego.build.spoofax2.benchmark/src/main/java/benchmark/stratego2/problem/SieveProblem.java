package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface SieveProblem extends Problem {

    @Override
    default String problemFileName() {
        return "sieve.str2";
    }
}
