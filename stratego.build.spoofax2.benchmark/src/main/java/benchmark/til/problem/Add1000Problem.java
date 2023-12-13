package benchmark.til.problem;

import benchmark.generic.Problem;

public interface Add1000Problem extends Problem {
    @Override
    default String problemFileName() {
        return "add1000.til";
    }
}
