package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface GarbageCollectionProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "garbagecollection.str2";
    }

}
