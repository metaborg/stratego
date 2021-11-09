package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BenchsymProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchsym%d.str2";
    }
}
