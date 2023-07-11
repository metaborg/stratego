package benchmark.til.problem;

import benchmark.generic.Problem;

public interface Add100Problem extends Problem {
    @Override
    default String problemFileName() {
        return "add100.til";
    }
}
