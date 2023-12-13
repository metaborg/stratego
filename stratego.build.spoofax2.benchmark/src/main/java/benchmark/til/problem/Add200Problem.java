package benchmark.til.problem;

import benchmark.generic.Problem;

public interface Add200Problem extends Problem {
    @Override
    default String problemFileName() {
        return "add200.til";
    }
}
