package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface BenchsymProblem extends Problem {

    @Override
    default String problemFileName() {
        return "benchsym.str2";
    }
}
