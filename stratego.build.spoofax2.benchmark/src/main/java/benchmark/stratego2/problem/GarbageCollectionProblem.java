package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface GarbageCollectionProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "garbagecollection.str2";
    }

}
