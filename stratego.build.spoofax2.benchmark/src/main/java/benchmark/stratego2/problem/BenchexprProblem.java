package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BenchexprProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchexpr%d.str2";
    }
}
