package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface SieveProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "sieve.py";
    }
}
