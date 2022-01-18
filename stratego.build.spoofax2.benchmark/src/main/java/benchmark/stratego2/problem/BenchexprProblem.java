package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BenchexprProblem extends Problem {

    @Override
    default String problemFileName() {
        return "benchexpr.str2";
    }
}
