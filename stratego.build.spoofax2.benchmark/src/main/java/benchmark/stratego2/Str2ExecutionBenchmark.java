package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import benchmark.stratego2.problems.ExecutableStr2Problem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static benchmark.stratego2.Str2BenchmarkUtil.initProgram;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 10, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class Str2ExecutionBenchmark {
    @Param({
            "Benchexpr_10",
            "Benchexpr_11",
            "Benchexpr_12",
            "Benchexpr_13",
//            "Benchexpr_14",
//            "Benchexpr_15",
//            "Benchexpr_16",
//            "Benchexpr_17",

            "Benchsym_10",
            "Benchsym_11",
            "Benchsym_12",
            "Benchsym_13",
//            "Benchsym_14",
//            "Benchsym_15",
//            "Benchsym_16",
//            "Benchsym_17",
//            "Benchsym_18",

            "Benchtree_2",
            "Benchtree_4",
//            "Benchtree_6",

            "Bubblesort_10",
            "Bubblesort_20",
            "Bubblesort_50",
//            "Bubblesort_100",
//            "Bubblesort_200",

            "Calls",

            "Factorial_4",
            "Factorial_5",
            "Factorial_6",
            "Factorial_7",

            "Fibonacci_18",
            "Fibonacci_19",
            "Fibonacci_20",
            "Fibonacci_21",

            "GarbageCollection",

            "Hanoi_4",
            "Hanoi_5",
            "Hanoi_6",
            "Hanoi_7",
            "Hanoi_8",
//            "Hanoi_9",
//            "Hanoi_10",
//            "Hanoi_11",

            "Mergesort_10",
            "Mergesort_20",
            "Mergesort_30",
//            "Mergesort_40",

            "Quicksort_10",
            "Quicksort_12",
            "Quicksort_14",
//            "Quicksort_16",
//            "Quicksort_18",
//            "Quicksort_20",

            "Sieve_20",
            "Sieve_40",
            "Sieve_60",
//            "Sieve_80",
//            "Sieve_100",
    })
    public ExecutableStr2Problem problem;

    @Param({"2"})
    public int optimisationLevel = -1;

    Stratego2Program program;

    @Setup(Level.Trial)
    public final void setup() throws MetaborgException, IOException {
        program = initProgram(problem, optimisationLevel);
        program.compileStratego();
        program.compileJava();
    }

    @TearDown(Level.Trial)
    public final void teardown() throws MetaborgException, IOException {
        program.cleanup();
    }

    @Benchmark
    public final String executeStratego() throws IOException, InterruptedException {
        return program.run(problem.input);
    }
}
