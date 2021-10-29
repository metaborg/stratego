package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface BenchexprProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchexpr%d.str2";
    }
}
