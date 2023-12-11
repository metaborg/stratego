package benchmark.chocopy.problem;

import benchmark.generic.Problem;

public interface GoingUpMultipleFramesProblem extends Problem {
    @Override
    default String problemFileName() {
        return "going_up_multiple_frames.py";
    }
}
