package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import api.til.TILProgram;
import benchmark.stratego2.problems.ExecutableStr2Problem;
import benchmark.til.problems.ExecutableTILProblem;
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

import static benchmark.stratego2.problems.ExecutableStr2Problem.Factorial4;
import static benchmark.stratego2.problems.ExecutableStr2Problem.*;
import static benchmark.til.problems.ExecutableTILProblem.Factorial9;
import static benchmark.til.problems.ExecutableTILProblem.*;

public class StrategoBenchmarkTests {
    private final Integer[] optimisationLevels = {3, 4};
    private final Collection<ExecutableStr2Problem> strategoProblems = new LinkedList<>(Arrays.asList(
            Benchexpr10,
            Benchsym10,
            Benchtree10,
            Bubblesort10,
            Calls,
            Factorial4,
            Fibonacci18,
            GarbageCollection,
            Hanoi4,
            Mergesort10,
            Quicksort10));//, Sieve20)); Sieve test fails to terminate in Docker
    private final Collection<ExecutableTILProblem> tilExecutionProblems = new LinkedList<>(Arrays.asList(
            Add100,
            Add200,
            Add500,
            Add1000,
            EBlock,
            Factorial9));

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

    Stream<DynamicTest> TILExecutionBenchmarkTests() {
        return tilExecutionProblems.stream().flatMap(problem -> {
            final AtomicReference<String> result = new AtomicReference<>();
            return Arrays.stream(optimisationLevels).map(
                    optimisationLevel -> {

                        Path sourcePath = Paths.get("src", "main", "resources", "til", problem.name + ".str2");
                        String MetaborgVersion = "2.6.0-SNAPSHOT";
                        try {
                            TILProgram program = new TILProgram(sourcePath, optimisationLevel, MetaborgVersion);
                            return DynamicTest.dynamicTest(
                                    "Compile & run " + problem.name + " -O"
                                            + optimisationLevel, () -> {
                                        try {
                                            final String localResult = program.run(problem.input);
                                            if (!result.compareAndSet(null, localResult)) {
                                                Assertions.assertEquals(result.get(), localResult);
                                            }
                                        } catch (MetaborgException e) {
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
