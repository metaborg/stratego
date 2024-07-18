package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface PrimeProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "prime.py";
    }
}
