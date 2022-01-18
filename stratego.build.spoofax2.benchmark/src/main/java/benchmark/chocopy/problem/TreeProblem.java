package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface TreeProblem extends Problem {
    @Override
    default String problemFileName() {
        return "tree.py";
    }
}
