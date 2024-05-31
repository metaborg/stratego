package benchmark.til.problem;

import benchmark.generic.Problem;

public interface Add500Problem extends Problem {
    @Override
    default String problemFileName() {
        return "add500.til";
    }
}
