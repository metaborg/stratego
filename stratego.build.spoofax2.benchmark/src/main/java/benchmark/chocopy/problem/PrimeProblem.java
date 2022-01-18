package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface PrimeProblem extends Problem {
    @Override
    default String problemFileName() {
        return "prime.py";
    }
}
