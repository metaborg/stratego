package benchmark.til.execution;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openjdk.jmh.annotations.Param;

import benchmark.til.problem.EBlockProblem;

public class EBlock extends TILExecutionBenchmark implements EBlockProblem {
    @Override protected Collection<String> input() {
        return Arrays.asList("\"1\"", "\"1\"");
    }
}
