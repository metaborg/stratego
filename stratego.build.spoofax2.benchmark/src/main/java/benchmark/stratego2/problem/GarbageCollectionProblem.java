package benchmark.stratego2.problem;

import benchmark.generic.Problem;

public interface GarbageCollectionProblem extends Problem {

    @Override
    default String problemFileName() {
        return "garbagecollection.str2";
    }

}
