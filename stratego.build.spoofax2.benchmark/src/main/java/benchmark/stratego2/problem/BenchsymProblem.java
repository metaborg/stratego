package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface BenchsymProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchsym%d.str2";
    }
}
