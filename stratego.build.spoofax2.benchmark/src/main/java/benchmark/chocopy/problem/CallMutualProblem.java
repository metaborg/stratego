package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface CallMutualProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "call_mutual.py";
    }
}
