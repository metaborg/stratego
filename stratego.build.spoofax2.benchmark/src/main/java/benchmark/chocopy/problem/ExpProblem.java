package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface ExpProblem extends Problem {
    @Override
    default String problemFileName() {
        return "exp.py";
    }
}
