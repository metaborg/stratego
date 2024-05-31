package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface CallsProblem extends Problem {

    @Override
    default String problemFileName() {
        return "calls.str2";
    }
}
