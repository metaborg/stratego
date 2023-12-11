package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface StdlibProblem extends Problem {
    @Override
    default String problemFileName() {
        return "stdlib.py";
    }
}
