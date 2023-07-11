package benchmark.til.problem;

import benchmark.generic.Problem;

public interface EBlockProblem extends Problem {
    @Override
    default String problemFileName() {
        return "eblock.til";
    }
}
