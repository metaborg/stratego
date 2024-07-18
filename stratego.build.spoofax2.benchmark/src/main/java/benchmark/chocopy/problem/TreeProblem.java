package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface TreeProblem extends Problem {
    @Override
    default String problemFileNamePattern() {
        return "tree.py";
    }
}
