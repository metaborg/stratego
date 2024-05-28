package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import benchmark.stratego2.problems.ExecutableStr2Problem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static benchmark.stratego2.problems.ExecutableStr2Problem.*;

public class StrategoBenchmarkTests {
    private final Integer[] optimisationLevels = {3, 4};
    private final Collection<ExecutableStr2Problem> strategoProblems = new LinkedList<>(Arrays.asList(
            Benchexpr_10,
            Benchsym_10,
            Benchtree_10,
            Bubblesort_10,
            Calls,
            Factorial_4,
            Fibonacci_18,
            GarbageCollection,
            Hanoi_4,
            Mergesort_10,
            Quicksort_10));//, Sieve_20)); Sieve test fails to terminate in Docker

    @TestFactory
    Stream<DynamicTest> strategoExecutionBenchmarkTests() {
        return strategoProblems.stream().flatMap(problem -> {
            final AtomicReference<String> result = new AtomicReference<>();
            return Arrays.stream(optimisationLevels).map(
                    optimisationLevel -> {
                        Path sourcePath = Paths.get("src", "main", "resources", "stratego2", problem.name + ".str2");
                        String MetaborgVersion = "2.6.0-SNAPSHOT";
                        Arguments args = new Arguments();
                        args.add("-O", optimisationLevel);
                        args.add("-sc", "on");
                        try {
                            Stratego2Program program = new Stratego2Program(sourcePath, args, MetaborgVersion);
                            return DynamicTest.dynamicTest(
                                    "Compile & run " + problem.name + " -O"
                                            + optimisationLevel, () -> {
                                        try {
                                            program.compileStratego();
                                            program.compileJava();
                                            final String localResult = program.run(problem.input);
                                            if (!result.compareAndSet(null, localResult)) {
                                                Assertions.assertEquals(result.get(), localResult);
                                            }
                                        } catch (IOException | MetaborgException | InterruptedException e) {
                                            throw new RuntimeException(e);
                                        } finally {
                                            program.cleanup();
                                        }
                                    });
                        } catch (IOException | MetaborgException e) {
                            throw new RuntimeException(e);
                        }
                    });
        });
    }
}
