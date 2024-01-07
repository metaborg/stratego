package benchmark.til;

import api.til.TILProgram;
import benchmark.til.problems.ExecutableTILProblem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.metaborg.core.MetaborgException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static benchmark.til.problems.ExecutableTILProblem.*;

public class TILBenchmarkTests {
    private final Integer[] optimisationLevels = {3, 4};
    private final Collection<ExecutableTILProblem> tilExecutionProblems = new LinkedList<>(Arrays.asList(
            Add_100,
            Add_200,
            Add_500,
            Add_1000,
            EBlock,
            Factorial_9));

//    @TestFactory
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
