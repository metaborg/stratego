package benchmark.stratego2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.metaborg.core.MetaborgException;

import benchmark.exception.InvalidConfigurationException;
import benchmark.exception.SkipException;
import benchmark.generic.OptimisationBenchmark;
import benchmark.stratego2.execution.Benchexpr;
import benchmark.stratego2.execution.Benchsym;
import benchmark.stratego2.execution.Benchtree;
import benchmark.stratego2.execution.Bubblesort;
import benchmark.stratego2.execution.Calls;
import benchmark.stratego2.execution.Factorial;
import benchmark.stratego2.execution.Fibonacci;
import benchmark.stratego2.execution.GarbageCollection;
import benchmark.stratego2.execution.Hanoi;
import benchmark.stratego2.execution.Mergesort;
import benchmark.stratego2.execution.Quicksort;
import benchmark.stratego2.execution.Sieve;
import benchmark.stratego2.execution.StrategoExecutionBenchmark;
import benchmark.til.execution.Add100;
import benchmark.til.execution.Add1000;
import benchmark.til.execution.Add200;
import benchmark.til.execution.Add500;
import benchmark.til.execution.TILExecutionBenchmark;

public class StrategoBenchmarkTests {
    private final Integer[] optimisationLevels = { 3, 4 };
    private final Collection<Class<? extends StrategoExecutionBenchmark>> strategoProblems =
        new LinkedList<>(
            Arrays.asList(Benchexpr.class, Benchsym.class, Benchtree.class, Bubblesort.class,
                Calls.class, Factorial.class, Fibonacci.class, GarbageCollection.class, Hanoi.class,
                Mergesort.class, Quicksort.class));//, Sieve.class));// Sieve test fails to terminate in Docker
    private final Collection<Class<? extends TILExecutionBenchmark>> tilExecutionProblems =
        new LinkedList<>(
            Arrays.asList(benchmark.til.execution.Factorial.class, benchmark.til.execution.EBlock.class,
                Add100.class, Add200.class, Add500.class, Add1000.class));

    @TestFactory Stream<DynamicTest> strategoExecutionBenchmarkTests() {
        return strategoProblems.stream().flatMap(problemClass -> {
            final AtomicReference<String> result = new AtomicReference<>();
            return Arrays.stream(optimisationLevels).map(
                optimisationLevel -> {
                    final StrategoExecutionBenchmark benchmark;
                    try {
                        benchmark = problemClass.getDeclaredConstructor().newInstance();
                    } catch(InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        benchmark.setMetaborgVersion("2.6.0-SNAPSHOT");
                        benchmark.setOptimisationLevel(optimisationLevel);
                        benchmark.setSharedConstructors("on");
                        // set shadowing fields in non-BaseBenchmark
                        benchmark.optimisationLevel = optimisationLevel;

                        benchmark.setup();
                    } catch(SkipException e) {
                        throw new RuntimeException(e);
                    } catch(InvalidConfigurationException e) {
                        return null;
                    }
                    return DynamicTest.dynamicTest(
                        "Compile & run " + benchmark.problemFileName() + " -O"
                            + optimisationLevel, () -> {
                            try {
                                benchmark.compile();
                                benchmark.setInput();

                                final String localResult = benchmark.run();
                                if(!result.compareAndSet(null, localResult)) {
                                    Assertions.assertEquals(result.get(), localResult);
                                }
                            } catch(IOException | MetaborgException | InterruptedException e) {
                                throw new RuntimeException(e);
                            } finally {
                                benchmark.teardown();
                            }
                        });
                }).filter(Objects::nonNull);
        });
    }

    @TestFactory Stream<DynamicTest> TILExecutionBenchmarkTests() {
        return tilExecutionProblems.stream().flatMap(problemClass -> {
            final AtomicReference<String> result = new AtomicReference<>();
            return Arrays.stream(optimisationLevels).map(
                optimisationLevel -> {
                        final TILExecutionBenchmark benchmark;
                        try {
                            benchmark = problemClass.newInstance();
                        } catch(InstantiationException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            benchmark.setMetaborgVersion("2.6.0-SNAPSHOT");
                            benchmark.setOptimisationLevel(optimisationLevel);
                            benchmark.setSharedConstructors("on");
                            // set shadowing fields in non-BaseBenchmark
                            benchmark.optimisationLevel = optimisationLevel;

                            benchmark.setup();
                        } catch(SkipException e) {
                            throw new RuntimeException(e);
                        } catch(InvalidConfigurationException e) {
                            return null;
                        }
                        return DynamicTest.dynamicTest(
                            "Compile & run " + benchmark.problemFileName() + " -O"
                                + optimisationLevel, () -> {
                                try {
                                    benchmark.setInput();

                                    final String localResult = benchmark.run();
                                    if(!result.compareAndSet(null, localResult)) {
                                        Assertions.assertEquals(result.get(), localResult);
                                    }
                                } catch(MetaborgException e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    benchmark.teardown();
                                }
                            });
                    }).filter(Objects::nonNull);
        });
    }
}
