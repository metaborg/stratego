package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface ExpProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "exp.py";
    }
}
