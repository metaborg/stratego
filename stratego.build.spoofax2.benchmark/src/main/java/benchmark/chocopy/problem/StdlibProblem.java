package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface StdlibProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "stdlib.py";
    }
}
